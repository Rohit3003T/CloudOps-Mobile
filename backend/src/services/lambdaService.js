const {
  ListFunctionsCommand,
  GetFunctionCommand,
  GetFunctionConfigurationCommand,
} = require('@aws-sdk/client-lambda');
const { lambdaClient } = require('../aws/clients');

function mapFunction(fn) {
  return {
    functionName: fn.FunctionName,
    functionArn:  fn.FunctionArn,
    runtime:      fn.Runtime,
    handler:      fn.Handler,
    codeSize:     fn.CodeSize,
    description:  fn.Description || '',
    lastModified: fn.LastModified,
    memorySize:   fn.MemorySize,
    timeout:      fn.Timeout,
    state:        fn.State || 'Active',
    role:         fn.Role,
    environment:  fn.Environment?.Variables
      ? Object.keys(fn.Environment.Variables) // keys only, never values
      : [],
    layers: (fn.Layers || []).map(l => l.Arn),
    ephemeralStorage: fn.EphemeralStorage?.Size || 512,
    packageType: fn.PackageType || 'Zip',
    architectures: fn.Architectures || ['x86_64'],
  };
}

async function listFunctions(maxItems = 50) {
  const resp = await lambdaClient.send(new ListFunctionsCommand({ MaxItems: maxItems }));
  return (resp.Functions || []).map(mapFunction);
}

async function getFunctionDetails(functionName) {
  const [configResp, fnResp] = await Promise.all([
    lambdaClient.send(new GetFunctionConfigurationCommand({ FunctionName: functionName })),
    lambdaClient.send(new GetFunctionCommand({ FunctionName: functionName })),
  ]);

  return {
    ...mapFunction(configResp),
    codeLocation:    fnResp.Code?.Location      || null,   // presigned URL (temp)
    repositoryType:  fnResp.Code?.RepositoryType || null,
    concurrency:     fnResp.Concurrency?.ReservedConcurrentExecutions ?? null,
    tags:            fnResp.Tags || {},
  };
}

module.exports = { listFunctions, getFunctionDetails };
