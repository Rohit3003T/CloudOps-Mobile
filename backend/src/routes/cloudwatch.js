const express = require('express');
const { GetMetricStatisticsCommand, ListMetricsCommand } = require('@aws-sdk/client-cloudwatch');
const { authenticate } = require('../middleware/auth');
const { getCloudWatchClient } = require('../services/awsClient');

const router = express.Router();

// Get CPU utilization for an EC2 instance
router.get('/cpu/:instanceId', authenticate, async (req, res) => {
  try {
    const cw = getCloudWatchClient(req.userId);
    const end = new Date();
    const start = new Date(end.getTime() - 3 * 60 * 60 * 1000); // last 3 hours

    const data = await cw.send(new GetMetricStatisticsCommand({
      Namespace: 'AWS/EC2',
      MetricName: 'CPUUtilization',
      Dimensions: [{ Name: 'InstanceId', Value: req.params.instanceId }],
      StartTime: start,
      EndTime: end,
      Period: 300, // 5-minute intervals
      Statistics: ['Average', 'Maximum'],
      Unit: 'Percent',
    }));

    const datapoints = (data.Datapoints || [])
      .sort((a, b) => new Date(a.Timestamp) - new Date(b.Timestamp))
      .map(dp => ({
        timestamp: dp.Timestamp,
        average: Math.round(dp.Average * 100) / 100,
        maximum: Math.round(dp.Maximum * 100) / 100,
      }));

    res.json({ instanceId: req.params.instanceId, metric: 'CPUUtilization', datapoints });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Get overview metrics summary
router.get('/overview', authenticate, async (req, res) => {
  try {
    const cw = getCloudWatchClient(req.userId);
    const end = new Date();
    const start = new Date(end.getTime() - 60 * 60 * 1000); // last 1 hour

    const metrics = ['CPUUtilization'];
    const results = {};

    // Get average CPU across all instances
    try {
      const data = await cw.send(new GetMetricStatisticsCommand({
        Namespace: 'AWS/EC2',
        MetricName: 'CPUUtilization',
        StartTime: start,
        EndTime: end,
        Period: 3600,
        Statistics: ['Average'],
        Unit: 'Percent',
      }));
      const avg = data.Datapoints?.[0]?.Average;
      results.avgCPU = avg ? Math.round(avg * 100) / 100 : null;
    } catch (_) {
      results.avgCPU = null;
    }

    res.json(results);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

module.exports = router;
