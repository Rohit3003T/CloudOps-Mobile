'use strict';

/** Generate a time series of fake metric datapoints going back `hoursBack` hours */
function generateDatapoints(metricName, hoursBack = 3, periodSecs = 300) {
  const points = [];
  const now = Date.now();
  const totalPoints = Math.floor((hoursBack * 3600) / periodSecs);

  for (let i = totalPoints; i >= 0; i--) {
    const ts = new Date(now - i * periodSecs * 1000);
    let value;
    switch (metricName) {
      case 'CPUUtilization': value = 20 + Math.random() * 40; break;
      case 'NetworkIn':      value = 50000 + Math.random() * 200000; break;
      case 'NetworkOut':     value = 30000 + Math.random() * 150000; break;
      case 'DiskReadBytes':  value = 10000 + Math.random() * 50000;  break;
      case 'DiskWriteBytes': value = 5000  + Math.random() * 30000;  break;
      default:               value = Math.random() * 100;
    }
    points.push({ timestamp: ts.toISOString(), value: parseFloat(value.toFixed(4)), unit: metricUnits[metricName] || 'None' });
  }
  return points;
}

const metricUnits = {
  CPUUtilization: 'Percent',
  NetworkIn:      'Bytes',
  NetworkOut:     'Bytes',
  DiskReadBytes:  'Bytes',
  DiskWriteBytes: 'Bytes',
};

const AVAILABLE_METRICS = [
  'CPUUtilization', 'NetworkIn', 'NetworkOut', 'NetworkPacketsIn', 'NetworkPacketsOut',
  'DiskReadBytes', 'DiskWriteBytes', 'DiskReadOps', 'DiskWriteOps',
  'StatusCheckFailed', 'StatusCheckFailed_Instance', 'StatusCheckFailed_System',
  'MetadataNoToken',
];

module.exports = {
  ec2Overview: (instanceId) => ({
    success: true, mock: true, instanceId, periodSecs: 300, hoursBack: 3,
    metrics: {
      cpuUtilization: { instanceId, metricName: 'CPUUtilization', stat: 'Average', periodSecs: 300, hoursBack: 3, unit: 'Percent', datapoints: generateDatapoints('CPUUtilization') },
      networkIn:      { instanceId, metricName: 'NetworkIn',      stat: 'Sum',     periodSecs: 300, hoursBack: 3, unit: 'Bytes',   datapoints: generateDatapoints('NetworkIn')      },
      networkOut:     { instanceId, metricName: 'NetworkOut',     stat: 'Sum',     periodSecs: 300, hoursBack: 3, unit: 'Bytes',   datapoints: generateDatapoints('NetworkOut')     },
      diskReadBytes:  { instanceId, metricName: 'DiskReadBytes',  stat: 'Sum',     periodSecs: 300, hoursBack: 3, unit: 'Bytes',   datapoints: generateDatapoints('DiskReadBytes')  },
      diskWriteBytes: { instanceId, metricName: 'DiskWriteBytes', stat: 'Sum',     periodSecs: 300, hoursBack: 3, unit: 'Bytes',   datapoints: generateDatapoints('DiskWriteBytes') },
    },
  }),
  singleMetric: (instanceId, metricName) => ({
    success: true, mock: true, instanceId, metricName, stat: 'Average',
    periodSecs: 300, hoursBack: 3, unit: metricUnits[metricName] || 'None',
    datapoints: generateDatapoints(metricName),
  }),
  availableMetrics: (instanceId) => ({
    success: true, mock: true, count: AVAILABLE_METRICS.length,
    metrics: AVAILABLE_METRICS.map(m => ({ metricName: m, namespace: 'AWS/EC2' })),
  }),
};
