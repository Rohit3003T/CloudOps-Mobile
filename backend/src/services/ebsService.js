const {
  DescribeVolumesCommand,
  CreateVolumeCommand,
  AttachVolumeCommand,
  DetachVolumeCommand,
  DescribeAvailabilityZonesCommand,
} = require('@aws-sdk/client-ec2');
const { ec2Client } = require('../aws/clients');

function mapVolume(v) {
  return {
    volumeId:         v.VolumeId,
    state:            v.State,
    size:             v.Size,           // GiB
    volumeType:       v.VolumeType,
    iops:             v.Iops           || null,
    throughput:       v.Throughput     || null,
    encrypted:        v.Encrypted      || false,
    snapshotId:       v.SnapshotId     || null,
    availabilityZone: v.AvailabilityZone,
    createTime:       v.CreateTime,
    multiAttachEnabled: v.MultiAttachEnabled || false,
    attachments: (v.Attachments || []).map(a => ({
      instanceId:  a.InstanceId,
      device:      a.Device,
      state:       a.State,
      attachTime:  a.AttachTime,
      deleteOnTermination: a.DeleteOnTermination,
    })),
    tags: (v.Tags || []).reduce((acc, t) => { acc[t.Key] = t.Value; return acc; }, {}),
  };
}

async function listVolumes(filters = {}) {
  const params = { Filters: [] };
  if (filters.state)    params.Filters.push({ Name: 'status',      Values: [filters.state] });
  if (filters.volumeId) params.VolumeIds = [filters.volumeId];

  const resp = await ec2Client.send(new DescribeVolumesCommand(params));
  return (resp.Volumes || []).map(mapVolume);
}

/**
 * Create a gp2 volume (free tier: up to 30 GiB total per month).
 * Defaults to 8 GiB gp2 in the configured region's first AZ.
 */
async function createVolume({ size = 8, volumeType = 'gp2', availabilityZone, encrypted = false, tags = {} } = {}) {
  // Resolve AZ if not provided
  if (!availabilityZone) {
    const azResp = await ec2Client.send(new DescribeAvailabilityZonesCommand({
      Filters: [{ Name: 'state', Values: ['available'] }],
    }));
    availabilityZone = azResp.AvailabilityZones?.[0]?.ZoneName;
    if (!availabilityZone) throw new Error('No available availability zones found');
  }

  const tagSpecs = Object.keys(tags).length
    ? [{ ResourceType: 'volume', Tags: Object.entries(tags).map(([k, v]) => ({ Key: k, Value: v })) }]
    : undefined;

  const params = {
    Size: size,
    VolumeType: volumeType,
    AvailabilityZone: availabilityZone,
    Encrypted: encrypted,
    ...(tagSpecs && { TagSpecifications: tagSpecs }),
  };

  const resp = await ec2Client.send(new CreateVolumeCommand(params));
  return mapVolume(resp);
}

async function attachVolume(volumeId, instanceId, device = '/dev/xvdf') {
  const resp = await ec2Client.send(new AttachVolumeCommand({
    VolumeId: volumeId, InstanceId: instanceId, Device: device,
  }));
  return {
    volumeId:   resp.VolumeId,
    instanceId: resp.InstanceId,
    device:     resp.Device,
    state:      resp.State,
    attachTime: resp.AttachTime,
  };
}

async function detachVolume(volumeId, instanceId, force = false) {
  const params = { VolumeId: volumeId, Force: force };
  if (instanceId) params.InstanceId = instanceId;

  const resp = await ec2Client.send(new DetachVolumeCommand(params));
  return {
    volumeId:   resp.VolumeId,
    instanceId: resp.InstanceId || null,
    device:     resp.Device     || null,
    state:      resp.State,
  };
}

module.exports = { listVolumes, createVolume, attachVolume, detachVolume };
