'use strict';

const BASE_FUNCTIONS = [
  { functionName: 'api-auth-handler',    functionArn: 'arn:aws:lambda:us-east-1:123456789012:function:api-auth-handler',    runtime: 'nodejs18.x',  handler: 'index.handler',                codeSize: 45231,  description: 'Handles API authentication',    lastModified: new Date(Date.now() - 86400000 * 2).toISOString(),  memorySize: 256, timeout: 30,  state: 'Active', role: 'arn:aws:iam::123456789012:role/lambda-role', environment: ['JWT_SECRET', 'DB_HOST'], layers: [], ephemeralStorage: 512, packageType: 'Zip', architectures: ['x86_64'] },
  { functionName: 'image-processor',     functionArn: 'arn:aws:lambda:us-east-1:123456789012:function:image-processor',     runtime: 'python3.11',  handler: 'lambda_function.lambda_handler', codeSize: 128456, description: 'Processes uploaded images',      lastModified: new Date(Date.now() - 86400000 * 7).toISOString(),  memorySize: 512, timeout: 60,  state: 'Active', role: 'arn:aws:iam::123456789012:role/lambda-role', environment: ['S3_BUCKET'],            layers: [], ephemeralStorage: 512, packageType: 'Zip', architectures: ['x86_64'] },
  { functionName: 'scheduled-cleanup',   functionArn: 'arn:aws:lambda:us-east-1:123456789012:function:scheduled-cleanup',   runtime: 'nodejs18.x',  handler: 'cleanup.handler',              codeSize: 12048,  description: 'Scheduled database cleanup job', lastModified: new Date(Date.now() - 86400000 * 14).toISOString(), memorySize: 128, timeout: 300, state: 'Active', role: 'arn:aws:iam::123456789012:role/lambda-role', environment: ['DB_URL'],               layers: [], ephemeralStorage: 512, packageType: 'Zip', architectures: ['x86_64'] },
  { functionName: 'notification-sender', functionArn: 'arn:aws:lambda:us-east-1:123456789012:function:notification-sender', runtime: 'python3.11',  handler: 'notify.send',                  codeSize: 8192,   description: 'Sends push notifications',       lastModified: new Date(Date.now() - 86400000 * 1).toISOString(),  memorySize: 128, timeout: 15,  state: 'Active', role: 'arn:aws:iam::123456789012:role/lambda-role', environment: ['SNS_TOPIC_ARN'],        layers: [], ephemeralStorage: 512, packageType: 'Zip', architectures: ['x86_64'] },
];

module.exports = {
  listFunctions:   () => ({ success: true, mock: true, count: BASE_FUNCTIONS.length, functions: BASE_FUNCTIONS }),
  functionDetails: (name) => {
    const found = BASE_FUNCTIONS.find(f => f.functionName === name) || { ...BASE_FUNCTIONS[0], functionName: name };
    return { success: true, mock: true, function: { ...found, codeLocation: null, repositoryType: 'S3', concurrency: null, tags: { Env: 'production' } } };
  },
};
