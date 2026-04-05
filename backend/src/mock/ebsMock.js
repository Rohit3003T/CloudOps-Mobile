'use strict';

const VOLUMES = [
  { volumeId: 'vol-0abc1234def56789a', state: 'in-use',    size: 8,  volumeType: 'gp2', iops: 100, throughput: null, encrypted: false, snapshotId: 'snap-0abc1234', availabilityZone: 'us-east-1a', createTime: new Date(Date.now() - 86400000 * 5).toISOString(),  multiAttachEnabled: false, attachments: [{ instanceId: 'i-0abc123def456789a', device: '/dev/xvda', state: 'attached', attachTime: new Date(Date.now() - 86400000 * 5).toISOString(), deleteOnTermination: true }],  tags: { Name: 'web-server-root' } },
  { volumeId: 'vol-0def5678abc90123b', state: 'available', size: 20, volumeType: 'gp2', iops: 100, throughput: null, encrypted: true,  snapshotId: null,            availabilityZone: 'us-east-1b', createTime: new Date(Date.now() - 86400000 * 2).toISOString(),  multiAttachEnabled: false, attachments: [], tags: { Name: 'data-volume-spare' } },
  { volumeId: 'vol-0ghi9012jkl34567c', state: 'in-use',    size: 8,  volumeType: 'gp3', iops: 3000, throughput: 125, encrypted: true, snapshotId: 'snap-0def5678', availabilityZone: 'us-east-1c', createTime: new Date(Date.now() - 86400000 * 10).toISOString(), multiAttachEnabled: false, attachments: [{ instanceId: 'i-0789012abc345678c', device: '/dev/xvda', state: 'attached', attachTime: new Date(Date.now() - 86400000 * 10).toISOString(), deleteOnTermination: true }], tags: { Name: 'api-server-root' } },
];

module.exports = {
  listVolumes: () => ({ success: true, mock: true, count: VOLUMES.length, volumes: VOLUMES }),
};
