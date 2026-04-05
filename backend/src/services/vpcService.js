const {
  DescribeVpcsCommand,
  DescribeSubnetsCommand,
  DescribeSecurityGroupsCommand,
} = require('@aws-sdk/client-ec2');
const { ec2Client } = require('../aws/clients');

function tagsToObj(tags = []) {
  return tags.reduce((acc, t) => { acc[t.Key] = t.Value; return acc; }, {});
}

function nameFromTags(tags = []) {
  return tags.find(t => t.Key === 'Name')?.Value || null;
}

async function listVpcs() {
  const resp = await ec2Client.send(new DescribeVpcsCommand({}));
  return (resp.Vpcs || []).map(v => ({
    vpcId:           v.VpcId,
    state:           v.State,
    cidrBlock:       v.CidrBlock,
    isDefault:       v.IsDefault,
    dhcpOptionsId:   v.DhcpOptionsId,
    instanceTenancy: v.InstanceTenancy,
    name:            nameFromTags(v.Tags),
    tags:            tagsToObj(v.Tags),
  }));
}

async function listSubnets(vpcId) {
  const params = vpcId
    ? { Filters: [{ Name: 'vpc-id', Values: [vpcId] }] }
    : {};
  const resp = await ec2Client.send(new DescribeSubnetsCommand(params));
  return (resp.Subnets || []).map(s => ({
    subnetId:                s.SubnetId,
    vpcId:                   s.VpcId,
    state:                   s.State,
    cidrBlock:               s.CidrBlock,
    availabilityZone:        s.AvailabilityZone,
    availableIpAddressCount: s.AvailableIpAddressCount,
    defaultForAz:            s.DefaultForAz,
    mapPublicIpOnLaunch:     s.MapPublicIpOnLaunch,
    name:                    nameFromTags(s.Tags),
    tags:                    tagsToObj(s.Tags),
  }));
}

async function listSecurityGroups(vpcId) {
  const params = vpcId
    ? { Filters: [{ Name: 'vpc-id', Values: [vpcId] }] }
    : {};
  const resp = await ec2Client.send(new DescribeSecurityGroupsCommand(params));
  return (resp.SecurityGroups || []).map(sg => ({
    groupId:     sg.GroupId,
    groupName:   sg.GroupName,
    description: sg.Description,
    vpcId:       sg.VpcId,
    ownerId:     sg.OwnerId,
    inboundRules: (sg.IpPermissions || []).map(r => ({
      protocol:  r.IpProtocol,
      fromPort:  r.FromPort  ?? null,
      toPort:    r.ToPort    ?? null,
      ipRanges:  (r.IpRanges || []).map(ip => ip.CidrIp),
      ipv6Ranges: (r.Ipv6Ranges || []).map(ip => ip.CidrIpv6),
    })),
    outboundRules: (sg.IpPermissionsEgress || []).map(r => ({
      protocol:  r.IpProtocol,
      fromPort:  r.FromPort  ?? null,
      toPort:    r.ToPort    ?? null,
      ipRanges:  (r.IpRanges || []).map(ip => ip.CidrIp),
    })),
    tags: tagsToObj(sg.Tags),
  }));
}

module.exports = { listVpcs, listSubnets, listSecurityGroups };
