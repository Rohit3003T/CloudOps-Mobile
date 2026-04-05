'use strict';
const express = require('express');
const svc  = require('../services/cloudwatchService');
const { isCredentialError } = require('../aws/clients');
const MOCK = require('../mock/cloudwatchMock');

const router = express.Router();

// ── EC2 overview metrics (CPU + Network + Disk) ────────────────────────────────
router.get('/ec2/:instanceId', async (req, res, next) => {
  const { instanceId } = req.params;
  const period   = parseInt(req.query.period   || '300',  10);
  const hoursBack = parseInt(req.query.hoursBack || '3',   10);
  try {
    const data = await svc.getEc2Overview(instanceId, period, hoursBack);
    res.json({ success: true, ...data });
  } catch (err) {
    if (isCredentialError(err)) return res.json(MOCK.ec2Overview(instanceId));
    next(err);
  }
});

// ── Single EC2 metric ──────────────────────────────────────────────────────────
// GET /api/monitoring/ec2/:instanceId/metric?name=CPUUtilization&stat=Average&period=300&hoursBack=3
router.get('/ec2/:instanceId/metric', async (req, res, next) => {
  const { instanceId } = req.params;
  const { name, stat = 'Average', period = '300', hoursBack = '3' } = req.query;
  if (!name) return res.status(400).json({ error: 'Query param "name" is required (e.g. CPUUtilization)' });
  try {
    const data = await svc.getEc2Metric(instanceId, name, stat, parseInt(period, 10), parseInt(hoursBack, 10));
    res.json({ success: true, ...data });
  } catch (err) {
    if (isCredentialError(err)) return res.json(MOCK.singleMetric(instanceId, name));
    next(err);
  }
});

// ── List available metrics for an instance ─────────────────────────────────────
router.get('/ec2/:instanceId/metrics', async (req, res, next) => {
  try {
    const metrics = await svc.listEc2Metrics(req.params.instanceId);
    res.json({ success: true, count: metrics.length, metrics });
  } catch (err) {
    if (isCredentialError(err)) return res.json(MOCK.availableMetrics(req.params.instanceId));
    next(err);
  }
});

module.exports = router;
