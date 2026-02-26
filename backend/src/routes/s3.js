const express = require('express');
const { ListBucketsCommand, GetBucketLocationCommand, GetBucketAclCommand } = require('@aws-sdk/client-s3');
const { authenticate } = require('../middleware/auth');
const { getS3Client } = require('../services/awsClient');

const router = express.Router();

// List S3 buckets
router.get('/buckets', authenticate, async (req, res) => {
  try {
    const s3 = getS3Client(req.userId);
    const data = await s3.send(new ListBucketsCommand({}));

    const buckets = await Promise.all(
      (data.Buckets || []).map(async (bucket) => {
        let region = 'unknown';
        let isPublic = false;

        try {
          const loc = await s3.send(new GetBucketLocationCommand({ Bucket: bucket.Name }));
          region = loc.LocationConstraint || 'us-east-1';
        } catch (_) {}

        try {
          const acl = await s3.send(new GetBucketAclCommand({ Bucket: bucket.Name }));
          isPublic = acl.Grants?.some(
            g => g.Grantee?.URI === 'http://acs.amazonaws.com/groups/global/AllUsers'
          ) || false;
        } catch (_) {}

        return {
          name: bucket.Name,
          createdAt: bucket.CreationDate,
          region,
          isPublic,
        };
      })
    );

    res.json({ buckets, total: buckets.length });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
