const {
  GetMetricStatisticsCommand,
  ListMetricsCommand,
} = require('@aws-sdk/client-cloudwatch');
const { cwClient } = require('../aws/clients');

/**
 * Fetch a single CloudWatch metric's statistics for an EC2 instance.
 * @param {string} instanceId
 * @param {string} metricName  e.g. 'CPUUtilization'
 * @param {string} stat        e.g. 'Average' | 'Sum' | 'Maximum' | 'Minimum'
 * @param {number} periodSecs  granularity in seconds (min 60, must be multiple of 60)
 * @param {number} hoursBack   how many hours of history to fetch
 */
async function getEc2Metric(instanceId, metricName, stat = 'Average', periodSecs = 300, hoursBack = 3) {
  const endTime   = new Date();
  const startTime = new Date(endTime.getTime() - hoursBack * 3600 * 1000);

  const resp = await cwClient.send(new GetMetricStatisticsCommand({
    Namespace:  'AWS/EC2',
    MetricName: metricName,
    Dimensions: [{ Name: 'InstanceId', Value: instanceId }],
    StartTime:  startTime,
    EndTime:    endTime,
    Period:     periodSecs,
    Statistics: [stat],
  }));

  const datapoints = (resp.Datapoints || [])
    .sort((a, b) => new Date(a.Timestamp) - new Date(b.Timestamp))
    .map(dp => ({
      timestamp: dp.Timestamp,
      value:     dp[stat] ?? null,
      unit:      dp.Unit,
    }));

  return {
    instanceId,
    metricName,
    stat,
    periodSecs,
    hoursBack,
    unit: resp.Datapoints?.[0]?.Unit || null,
    datapoints,
  };
}

/**
 * Convenience: fetch CPU + Network In/Out in one call.
 */
async function getEc2Overview(instanceId, periodSecs = 300, hoursBack = 3) {
  const [cpu, netIn, netOut, diskRead, diskWrite] = await Promise.allSettled([
    getEc2Metric(instanceId, 'CPUUtilization',    'Average', periodSecs, hoursBack),
    getEc2Metric(instanceId, 'NetworkIn',          'Sum',    periodSecs, hoursBack),
    getEc2Metric(instanceId, 'NetworkOut',         'Sum',    periodSecs, hoursBack),
    getEc2Metric(instanceId, 'DiskReadBytes',      'Sum',    periodSecs, hoursBack),
    getEc2Metric(instanceId, 'DiskWriteBytes',     'Sum',    periodSecs, hoursBack),
  ]);

  return {
    instanceId,
    periodSecs,
    hoursBack,
    metrics: {
      cpuUtilization: cpu.status      === 'fulfilled' ? cpu.value      : null,
      networkIn:      netIn.status    === 'fulfilled' ? netIn.value    : null,
      networkOut:     netOut.status   === 'fulfilled' ? netOut.value   : null,
      diskReadBytes:  diskRead.status === 'fulfilled' ? diskRead.value : null,
      diskWriteBytes: diskWrite.status=== 'fulfilled' ? diskWrite.value: null,
    },
  };
}

/**
 * List available CloudWatch metrics for an EC2 instance.
 */
async function listEc2Metrics(instanceId) {
  const resp = await cwClient.send(new ListMetricsCommand({
    Namespace:  'AWS/EC2',
    Dimensions: [{ Name: 'InstanceId', Value: instanceId }],
  }));
  return (resp.Metrics || []).map(m => ({
    metricName: m.MetricName,
    namespace:  m.Namespace,
  }));
}

module.exports = { getEc2Metric, getEc2Overview, listEc2Metrics };
