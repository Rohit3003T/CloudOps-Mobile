const { STSClient, GetCallerIdentityCommand } = require('@aws-sdk/client-sts');
const { EC2Client } = require('@aws-sdk/client-ec2');
const { S3Client } = require('@aws-sdk/client-s3');
const { CloudWatchClient } = require('@aws-sdk/client-cloudwatch');
const { CostExplorerClient } = require('@aws-sdk/client-cost-explorer');
const { BudgetsClient } = require('@aws-sdk/client-budgets');
const { IAMClient } = require('@aws-sdk/client-iam');
const { awsCredentials } = require('../config/store');

const getCredentials = (userId) => {
  const creds = awsCredentials.get(userId);
  if (!creds) throw new Error('AWS credentials not configured. Please connect your AWS account first.');
  return creds;
};

const getSTSClient = (userId) => {
  const { accessKeyId, secretAccessKey, region } = getCredentials(userId);
  return new STSClient({ region, credentials: { accessKeyId, secretAccessKey } });
};

const getEC2Client = (userId) => {
  const { accessKeyId, secretAccessKey, region } = getCredentials(userId);
  return new EC2Client({ region, credentials: { accessKeyId, secretAccessKey } });
};

const getS3Client = (userId) => {
  const { accessKeyId, secretAccessKey, region } = getCredentials(userId);
  return new S3Client({ region, credentials: { accessKeyId, secretAccessKey } });
};

const getCloudWatchClient = (userId) => {
  const { accessKeyId, secretAccessKey, region } = getCredentials(userId);
  return new CloudWatchClient({ region, credentials: { accessKeyId, secretAccessKey } });
};

const getCostExplorerClient = (userId) => {
  const { accessKeyId, secretAccessKey } = getCredentials(userId);
  // Cost Explorer is only available in us-east-1
  return new CostExplorerClient({ region: 'us-east-1', credentials: { accessKeyId, secretAccessKey } });
};

const getBudgetsClient = (userId) => {
  const { accessKeyId, secretAccessKey } = getCredentials(userId);
  return new BudgetsClient({ region: 'us-east-1', credentials: { accessKeyId, secretAccessKey } });
};

const getIAMClient = (userId) => {
  const { accessKeyId, secretAccessKey } = getCredentials(userId);
  return new IAMClient({ region: 'us-east-1', credentials: { accessKeyId, secretAccessKey } });
};

module.exports = {
  getCredentials,
  getSTSClient,
  getEC2Client,
  getS3Client,
  getCloudWatchClient,
  getCostExplorerClient,
  getBudgetsClient,
  getIAMClient,
};
