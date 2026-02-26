const express = require('express');
const { DescribeInstancesCommand, StartInstancesCommand, StopInstancesCommand } = require('@aws-sdk/client-ec2');
const { authenticate } = require('../middleware/auth');
const { getEC2Client } = require('../services/awsClient');

const router = express.Router();

// List all EC2 instances
router.get('/instances', authenticate, async (req, res) => {
  try {
    const ec2 = getEC2Client(req.userId);
    const data = await ec2.send(new DescribeInstancesCommand({}));

    const instances = [];
    for (const reservation of data.Reservations || []) {
      for (const instance of reservation.Instances || []) {
        const nameTag = instance.Tags?.find(t => t.Key === 'Name');
        instances.push({
          instanceId: instance.InstanceId,
          name: nameTag?.Value || 'Unnamed',
          state: instance.State?.Name,
          instanceType: instance.InstanceType,
          publicIp: instance.PublicIpAddress || null,
          privateIp: instance.PrivateIpAddress || null,
          az: instance.Placement?.AvailabilityZone,
          launchTime: instance.LaunchTime,
          platform: instance.Platform || 'linux',
        });
      }
    }

    res.json({ instances, total: instances.length });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Get instance summary stats
router.get('/summary', authenticate, async (req, res) => {
  try {
    const ec2 = getEC2Client(req.userId);
    const data = await ec2.send(new DescribeInstancesCommand({}));

    let running = 0, stopped = 0, total = 0;
    for (const reservation of data.Reservations || []) {
      for (const instance of reservation.Instances || []) {
        total++;
        if (instance.State?.Name === 'running') running++;
        else if (instance.State?.Name === 'stopped') stopped++;
      }
    }

    res.json({ total, running, stopped, other: total - running - stopped });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
