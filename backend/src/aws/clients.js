const { EC2Client } = require('@aws-sdk/client-ec2');
const { S3Client } = require('@aws-sdk/client-s3');
const { LambdaClient } = require('@aws-sdk/client-lambda');
const { CostExplorerClient } = require('@aws-sdk/client-cost-explorer');
const { CloudWatchClient } = require('@aws-sdk/client-cloudwatch');

function buildClientConfig(regionOverride) {
  const region = regionOverride || process.env.AWS_REGION || 'us-east-1';
  if (process.env.AWS_ACCESS_KEY_ID && process.env.AWS_SECRET_ACCESS_KEY) {
    return {
      region,
      credentials: {
        accessKeyId: process.env.AWS_ACCESS_KEY_ID,
        secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY,
      },
    };
  }
  return { region };
}

const clientConfig = buildClientConfig();

const ec2Client    = new EC2Client(clientConfig);
const s3Client     = new S3Client(clientConfig);
const lambdaClient = new LambdaClient(clientConfig);
const cwClient     = new CloudWatchClient(clientConfig);
const costClient   = new CostExplorerClient(buildClientConfig('us-east-1'));

function hasAwsCredentials() {
  return !!(process.env.AWS_ACCESS_KEY_ID && process.env.AWS_SECRET_ACCESS_KEY);
}

function isCredentialError(err) {
  const names = [
    'CredentialsProviderError', 'InvalidClientTokenId',
    'ExpiredTokenException', 'NoCredentialProviders', 'AccessDeniedException',
  ];
  return names.includes(err.name) || !hasAwsCredentials();
}

module.exports = { ec2Client, s3Client, lambdaClient, costClient, cwClient, hasAwsCredentials, isCredentialError };
