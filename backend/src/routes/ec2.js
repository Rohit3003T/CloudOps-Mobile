'use strict';
const express = require('express');
const svc = require('../services/ec2Service');
const { isCredentialError } = require('../aws/clients');
const { destructiveLimiter } = require('../middleware/rateLimiter');
const MOCK = require('../mock/ec2Mock');

const router = express.Router();

// ── List all instances ─────────────────────────────────────────────────────────
router.get('/', async (req, res, next) => {
  try {
    const instances = await svc.listInstances({ state: req.query.state });
    res.json({ success: true, count: instances.length, instances });
  } catch (err) {
    if (isCredentialError(err)) return res.json(MOCK.listInstances());
    next(err);
  }
});

// ── Instance details ───────────────────────────────────────────────────────────
router.get('/:instanceId', async (req, res, next) => {
  try {
    const instance = await svc.getInstanceDetails(req.params.instanceId);
    res.json({ success: true, instance });
  } catch (err) {
    if (err.statusCode === 404) return res.status(404).json({ error: err.message });
    if (isCredentialError(err)) return res.json(MOCK.instanceDetails(req.params.instanceId));
    next(err);
  }
});

// ── Create t2.micro instance ───────────────────────────────────────────────────
router.post('/', async (req, res, next) => {
  try {
    const instance = await svc.createInstance(req.body);
    res.status(201).json({ success: true, instance });
  } catch (err) {
    if (isCredentialError(err)) return res.status(501).json({ error: 'AWS credentials not configured' });
    next(err);
  }
});

// ── Start ──────────────────────────────────────────────────────────────────────
router.post('/:instanceId/start', async (req, res, next) => {
  try {
    const result = await svc.startInstance(req.params.instanceId);
    res.json({ success: true, action: 'start', result });
  } catch (err) {
    if (isCredentialError(err)) return res.status(501).json({ error: 'AWS credentials not configured' });
    next(err);
  }
});

// ── Stop ───────────────────────────────────────────────────────────────────────
router.post('/:instanceId/stop', async (req, res, next) => {
  try {
    const result = await svc.stopInstance(req.params.instanceId, req.body.force === true);
    res.json({ success: true, action: 'stop', result });
  } catch (err) {
    if (isCredentialError(err)) return res.status(501).json({ error: 'AWS credentials not configured' });
    next(err);
  }
});

// ── Reboot ─────────────────────────────────────────────────────────────────────
router.post('/:instanceId/reboot', async (req, res, next) => {
  try {
    const result = await svc.rebootInstance(req.params.instanceId);
    res.json({ success: true, action: 'reboot', result });
  } catch (err) {
    if (isCredentialError(err)) return res.status(501).json({ error: 'AWS credentials not configured' });
    next(err);
  }
});

// ── Terminate (destructive — extra rate limit) ─────────────────────────────────
router.delete('/:instanceId', destructiveLimiter, async (req, res, next) => {
  try {
    const result = await svc.terminateInstance(req.params.instanceId);
    res.json({ success: true, action: 'terminate', result });
  } catch (err) {
    if (isCredentialError(err)) return res.status(501).json({ error: 'AWS credentials not configured' });
    next(err);
  }
});

module.exports = router;
