const express = require('express');
const { STSClient, GetCallerIdentityCommand } = require('@aws-sdk/client-sts');
const { authenticate } = require('../middleware/auth');
const { awsCredentials } = require('../config/store');

const router = express.Router();

// Connect AWS account (verify + save credentials)
router.post('/connect', authenticate, async (req, res) => {
  try {
    const { accessKeyId, secretAccessKey, region } = req.body;

    if (!accessKeyId || !secretAccessKey || !region) {
      return res.status(400).json({ error: 'accessKeyId, secretAccessKey, and region are required' });
    }

    // Verify credentials with STS
    const stsClient = new STSClient({
      region,
      credentials: { accessKeyId, secretAccessKey },
    });

    const identity = await stsClient.send(new GetCallerIdentityCommand({}));

    // Save credentials
    awsCredentials.set(req.userId, {
      accessKeyId,
      secretAccessKey,
      region,
      accountId: identity.Account,
      arn: identity.Arn,
      userId: identity.UserId,
    });

    res.json({
      message: 'AWS account connected successfully',
      account: {
        accountId: identity.Account,
        arn: identity.Arn,
        region,
      },
    });
  } catch (err) {
    if (err.name === 'InvalidClientTokenId' || err.name === 'SignatureDoesNotMatch') {
      return res.status(401).json({ error: 'Invalid AWS credentials. Please check your Access Key ID and Secret Access Key.' });
    }
    res.status(500).json({ error: err.message });
  }
});

// Get current AWS connection status
router.get('/status', authenticate, (req, res) => {
  const creds = awsCredentials.get(req.userId);
  if (!creds) {
    return res.json({ connected: false });
  }
  res.json({
    connected: true,
    accountId: creds.accountId,
    region: creds.region,
    arn: creds.arn,
  });
});

// Disconnect AWS account
router.delete('/disconnect', authenticate, (req, res) => {
  awsCredentials.delete(req.userId);
  res.json({ message: 'AWS account disconnected' });
});

module.exports = router;
