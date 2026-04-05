'use strict';

const BASE_INSTANCES = [
  {
    instanceId: 'i-0abc123def456789a', instanceType: 't2.micro', state: 'running',
    publicIp: '54.234.12.45', privateIp: '10.0.1.5', publicDns: 'ec2-54-234-12-45.compute-1.amazonaws.com',
    privateDns: 'ip-10-0-1-5.ec2.internal', launchTime: new Date(Date.now() - 86400000 * 5).toISOString(),
    availabilityZone: 'us-east-1a', subnetId: 'subnet-0abc1234', vpcId: 'vpc-0abc1234',
    imageId: 'ami-0c02fb55956c7d316', keyName: 'my-keypair', name: 'web-server-prod',
    platform: 'linux', architecture: 'x86_64', rootDeviceType: 'ebs',
    securityGroups: [{ id: 'sg-0abc1234', name: 'web-sg' }],
    tags: { Name: 'web-server-prod', Env: 'production' },
  },
  {
    instanceId: 'i-0def456abc789012b', instanceType: 't2.micro', state: 'stopped',
    publicIp: null, privateIp: '10.0.1.6', publicDns: null,
    privateDns: 'ip-10-0-1-6.ec2.internal', launchTime: new Date(Date.now() - 86400000 * 10).toISOString(),
    availabilityZone: 'us-east-1b', subnetId: 'subnet-0def5678', vpcId: 'vpc-0abc1234',
    imageId: 'ami-0c02fb55956c7d316', keyName: 'my-keypair', name: 'db-server-staging',
    platform: 'linux', architecture: 'x86_64', rootDeviceType: 'ebs',
    securityGroups: [{ id: 'sg-0def5678', name: 'db-sg' }],
    tags: { Name: 'db-server-staging', Env: 'staging' },
  },
  {
    instanceId: 'i-0789012abc345678c', instanceType: 't2.micro', state: 'running',
    publicIp: '52.10.34.67', privateIp: '10.0.2.10', publicDns: 'ec2-52-10-34-67.compute-1.amazonaws.com',
    privateDns: 'ip-10-0-2-10.ec2.internal', launchTime: new Date(Date.now() - 86400000 * 2).toISOString(),
    availabilityZone: 'us-east-1c', subnetId: 'subnet-0ghi9012', vpcId: 'vpc-0abc1234',
    imageId: 'ami-0c02fb55956c7d316', keyName: 'my-keypair', name: 'api-server-prod',
    platform: 'linux', architecture: 'x86_64', rootDeviceType: 'ebs',
    securityGroups: [{ id: 'sg-0abc1234', name: 'web-sg' }],
    tags: { Name: 'api-server-prod', Env: 'production' },
  },
];

module.exports = {
  listInstances: () => ({ success: true, mock: true, count: BASE_INSTANCES.length, instances: BASE_INSTANCES }),
  instanceDetails: (instanceId) => {
    const found = BASE_INSTANCES.find(i => i.instanceId === instanceId) || { ...BASE_INSTANCES[0], instanceId };
    return { success: true, mock: true, instance: found };
  },
};
