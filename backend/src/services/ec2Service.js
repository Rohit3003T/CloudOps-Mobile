const {
  DescribeInstancesCommand,
  StartInstancesCommand,
  StopInstancesCommand,
  RebootInstancesCommand,
  TerminateInstancesCommand,
  RunInstancesCommand,
} = require('@aws-sdk/client-ec2');
const { ec2Client } = require('../aws/clients');

// ── Helpers ────────────────────────────────────────────────────────────────────

function mapInstance(instance) {
  const nameTag = (instance.Tags || []).find(t => t.Key === 'Name');
  return {
    instanceId:       instance.InstanceId,
    instanceType:     instance.InstanceType,
    state:            instance.State?.Name || 'unknown',
    publicIp:         instance.PublicIpAddress  || null,
    privateIp:        instance.PrivateIpAddress || null,
    publicDns:        instance.PublicDnsName    || null,
    privateDns:       instance.PrivateDnsName   || null,
    launchTime:       instance.LaunchTime       || null,
    availabilityZone: instance.Placement?.AvailabilityZone || null,
    subnetId:         instance.SubnetId         || null,
    vpcId:            instance.VpcId            || null,
    imageId:          instance.ImageId          || null,
    keyName:          instance.KeyName          || null,
    name:             nameTag?.Value            || null,
    platform:         instance.Platform         || 'linux',
    architecture:     instance.Architecture     || null,
    rootDeviceType:   instance.RootDeviceType   || null,
    securityGroups:   (instance.SecurityGroups || []).map(sg => ({
      id: sg.GroupId, name: sg.GroupName,
    })),
    tags: (instance.Tags || []).reduce((acc, t) => {
      acc[t.Key] = t.Value; return acc;
    }, {}),
  };
}

// ── Service functions ──────────────────────────────────────────────────────────

async function listInstances(filters = {}) {
  const params = { Filters: [] };
  if (filters.state) {
    params.Filters.push({ Name: 'instance-state-name', Values: [filters.state] });
  }
  if (filters.instanceId) {
    params.InstanceIds = [filters.instanceId];
  }
  const resp = await ec2Client.send(new DescribeInstancesCommand(params));
  const instances = [];
  for (const r of resp.Reservations || []) {
    for (const i of r.Instances || []) instances.push(mapInstance(i));
  }
  return instances;
}

async function getInstanceDetails(instanceId) {
  const instances = await listInstances({ instanceId });
  if (!instances.length) throw Object.assign(new Error('Instance not found'), { statusCode: 404 });
  return instances[0];
}

async function startInstance(instanceId) {
  const resp = await ec2Client.send(new StartInstancesCommand({ InstanceIds: [instanceId] }));
  return resp.StartingInstances?.[0] || {};
}

async function stopInstance(instanceId, force = false) {
  const resp = await ec2Client.send(
    new StopInstancesCommand({ InstanceIds: [instanceId], Force: force })
  );
  return resp.StoppingInstances?.[0] || {};
}

async function rebootInstance(instanceId) {
  await ec2Client.send(new RebootInstancesCommand({ InstanceIds: [instanceId] }));
  return { instanceId, action: 'reboot', requested: new Date().toISOString() };
}

async function terminateInstance(instanceId) {
  const resp = await ec2Client.send(
    new TerminateInstancesCommand({ InstanceIds: [instanceId] })
  );
  return resp.TerminatingInstances?.[0] || {};
}

/**
 * Create a free-tier t2.micro instance.
 * All defaults come from env vars so the caller only needs to pass a name.
 */
async function createInstance({ name, amiId, keyName, subnetId, securityGroupId, userData } = {}) {
  const params = {
    ImageId:      amiId           || process.env.EC2_DEFAULT_AMI,
    InstanceType: 't2.micro',     // always free-tier
    MinCount: 1,
    MaxCount: 1,
    TagSpecifications: [{
      ResourceType: 'instance',
      Tags: [{ Key: 'Name', Value: name || 'cloud-monitor-instance' }],
    }],
  };

  if (keyName           || process.env.EC2_DEFAULT_KEY_PAIR)    params.KeyName          = keyName || process.env.EC2_DEFAULT_KEY_PAIR;
  if (subnetId          || process.env.EC2_DEFAULT_SUBNET_ID)   params.SubnetId         = subnetId || process.env.EC2_DEFAULT_SUBNET_ID;
  if (securityGroupId   || process.env.EC2_DEFAULT_SECURITY_GROUP_ID)
    params.SecurityGroupIds = [securityGroupId || process.env.EC2_DEFAULT_SECURITY_GROUP_ID];
  if (userData) params.UserData = Buffer.from(userData).toString('base64');

  const resp = await ec2Client.send(new RunInstancesCommand(params));
  return mapInstance(resp.Instances[0]);
}

module.exports = {
  listInstances,
  getInstanceDetails,
  startInstance,
  stopInstance,
  rebootInstance,
  terminateInstance,
  createInstance,
};
