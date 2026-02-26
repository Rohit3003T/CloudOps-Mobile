const express = require('express');
const { ListBucketsCommand, GetBucketAclCommand, GetPublicAccessBlockCommand } = require('@aws-sdk/client-s3');
const { DescribeSecurityGroupsCommand } = require('@aws-sdk/client-ec2');
const { authenticate } = require('../middleware/auth');
const { getS3Client, getEC2Client } = require('../services/awsClient');

const router = express.Router();

// Get security posture summary
router.get('/posture', authenticate, async (req, res) => {
  try {
    const s3 = getS3Client(req.userId);
    const ec2 = getEC2Client(req.userId);

    const findings = [];
    let score = 100;

    // Check S3 public buckets
    try {
      const bucketsData = await s3.send(new ListBucketsCommand({}));
      for (const bucket of bucketsData.Buckets || []) {
        try {
          const pab = await s3.send(new GetPublicAccessBlockCommand({ Bucket: bucket.Name }));
          const config = pab.PublicAccessBlockConfiguration;
          const allBlocked = config?.BlockPublicAcls && config?.BlockPublicPolicy &&
                             config?.IgnorePublicAcls && config?.RestrictPublicBuckets;
          if (!allBlocked) {
            findings.push({
              type: 'S3_PUBLIC_ACCESS',
              severity: 'HIGH',
              resource: bucket.Name,
              message: `S3 bucket "${bucket.Name}" may have public access enabled`,
            });
            score -= 15;
          }
        } catch (_) {
          // Could not check - flag as warning
          findings.push({
            type: 'S3_CHECK_FAILED',
            severity: 'LOW',
            resource: bucket.Name,
            message: `Could not verify public access settings for bucket "${bucket.Name}"`,
          });
          score -= 5;
        }
      }
    } catch (_) {}

    // Check security groups with open ports
    try {
      const sgData = await ec2.send(new DescribeSecurityGroupsCommand({}));
      for (const sg of sgData.SecurityGroups || []) {
        for (const rule of sg.IpPermissions || []) {
          const openToWorld = rule.IpRanges?.some(r => r.CidrIp === '0.0.0.0/0');
          const openToWorldV6 = rule.Ipv6Ranges?.some(r => r.CidrIpv6 === '::/0');
          if ((openToWorld || openToWorldV6) && rule.FromPort !== 443 && rule.FromPort !== 80) {
            findings.push({
              type: 'SG_OPEN_TO_WORLD',
              severity: rule.FromPort === 22 || rule.FromPort === 3389 ? 'CRITICAL' : 'MEDIUM',
              resource: `${sg.GroupName} (${sg.GroupId})`,
              message: `Security group "${sg.GroupName}" allows port ${rule.FromPort || 'ALL'} from 0.0.0.0/0`,
            });
            score -= rule.FromPort === 22 || rule.FromPort === 3389 ? 25 : 10;
          }
        }
      }
    } catch (_) {}

    score = Math.max(0, score);

    const posture = score >= 80 ? 'Good' : score >= 60 ? 'Fair' : score >= 40 ? 'Poor' : 'Critical';

    res.json({
      score,
      posture,
      findings,
      critical: findings.filter(f => f.severity === 'CRITICAL').length,
      high: findings.filter(f => f.severity === 'HIGH').length,
      medium: findings.filter(f => f.severity === 'MEDIUM').length,
      low: findings.filter(f => f.severity === 'LOW').length,
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
