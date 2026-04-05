'use strict';

module.exports = {
  listVpcs: () => ({
    success: true, mock: true, count: 2,
    vpcs: [
      { vpcId: 'vpc-0abc1234', state: 'available', cidrBlock: '10.0.0.0/16', isDefault: true,  dhcpOptionsId: 'dopt-0abc1234', instanceTenancy: 'default', name: 'default-vpc',  tags: { Name: 'default-vpc' } },
      { vpcId: 'vpc-0def5678', state: 'available', cidrBlock: '172.16.0.0/16', isDefault: false, dhcpOptionsId: 'dopt-0def5678', instanceTenancy: 'default', name: 'production-vpc', tags: { Name: 'production-vpc', Env: 'prod' } },
    ],
  }),
  listSubnets: () => ({
    success: true, mock: true, count: 3,
    subnets: [
      { subnetId: 'subnet-0abc1234', vpcId: 'vpc-0abc1234', state: 'available', cidrBlock: '10.0.1.0/24', availabilityZone: 'us-east-1a', availableIpAddressCount: 251, defaultForAz: true,  mapPublicIpOnLaunch: true,  name: 'public-subnet-1a',  tags: { Name: 'public-subnet-1a' } },
      { subnetId: 'subnet-0def5678', vpcId: 'vpc-0abc1234', state: 'available', cidrBlock: '10.0.2.0/24', availabilityZone: 'us-east-1b', availableIpAddressCount: 248, defaultForAz: true,  mapPublicIpOnLaunch: true,  name: 'public-subnet-1b',  tags: { Name: 'public-subnet-1b' } },
      { subnetId: 'subnet-0ghi9012', vpcId: 'vpc-0def5678', state: 'available', cidrBlock: '172.16.1.0/24', availabilityZone: 'us-east-1a', availableIpAddressCount: 245, defaultForAz: false, mapPublicIpOnLaunch: false, name: 'private-subnet-prod', tags: { Name: 'private-subnet-prod', Env: 'prod' } },
    ],
  }),
  listSecurityGroups: () => ({
    success: true, mock: true, count: 2,
    securityGroups: [
      { groupId: 'sg-0abc1234', groupName: 'web-sg',     description: 'Web server security group', vpcId: 'vpc-0abc1234', ownerId: '123456789012', inboundRules: [{ protocol: 'tcp', fromPort: 80, toPort: 80, ipRanges: ['0.0.0.0/0'], ipv6Ranges: [] }, { protocol: 'tcp', fromPort: 443, toPort: 443, ipRanges: ['0.0.0.0/0'], ipv6Ranges: [] }, { protocol: 'tcp', fromPort: 22, toPort: 22, ipRanges: ['10.0.0.0/8'], ipv6Ranges: [] }], outboundRules: [{ protocol: '-1', fromPort: null, toPort: null, ipRanges: ['0.0.0.0/0'] }], tags: { Name: 'web-sg' } },
      { groupId: 'sg-0def5678', groupName: 'db-sg',      description: 'Database security group',   vpcId: 'vpc-0abc1234', ownerId: '123456789012', inboundRules: [{ protocol: 'tcp', fromPort: 5432, toPort: 5432, ipRanges: ['10.0.0.0/16'], ipv6Ranges: [] }],                                                                                                                                                                                                     outboundRules: [{ protocol: '-1', fromPort: null, toPort: null, ipRanges: ['0.0.0.0/0'] }], tags: { Name: 'db-sg' } },
    ],
  }),
};
