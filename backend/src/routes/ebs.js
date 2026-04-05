'use strict';
const express = require('express');
const svc  = require('../services/ebsService');
const { isCredentialError } = require('../aws/clients');
const { destructiveLimiter } = require('../middleware/rateLimiter');
const MOCK = require('../mock/ebsMock');

const router = express.Router();

// ── List volumes ───────────────────────────────────────────────────────────────
router.get('/', async (req, res, next) => {
  try {
    const volumes = await svc.listVolumes({ state: req.query.state, volumeId: req.query.volumeId });
    res.json({ success: true, count: volumes.length, volumes });
  } catch (err) {
    if (isCredentialError(err)) return res.json(MOCK.listVolumes());
    next(err);
  }
});

// ── Create volume ──────────────────────────────────────────────────────────────
router.post('/', async (req, res, next) => {
  const { size, volumeType, availabilityZone, encrypted, tags } = req.body;
  try {
    const volume = await svc.createVolume({ size, volumeType, availabilityZone, encrypted, tags });
    res.status(201).json({ success: true, volume });
  } catch (err) {
    if (isCredentialError(err)) return res.status(501).json({ error: 'AWS credentials not configured' });
    next(err);
  }
});

// ── Attach volume ──────────────────────────────────────────────────────────────
router.post('/:volumeId/attach', async (req, res, next) => {
  const { instanceId, device } = req.body;
  if (!instanceId) return res.status(400).json({ error: 'instanceId is required' });
  try {
    const result = await svc.attachVolume(req.params.volumeId, instanceId, device);
    res.json({ success: true, action: 'attach', result });
  } catch (err) {
    if (isCredentialError(err)) return res.status(501).json({ error: 'AWS credentials not configured' });
    next(err);
  }
});

// ── Detach volume ──────────────────────────────────────────────────────────────
router.post('/:volumeId/detach', destructiveLimiter, async (req, res, next) => {
  const { instanceId, force } = req.body;
  try {
    const result = await svc.detachVolume(req.params.volumeId, instanceId, force === true);
    res.json({ success: true, action: 'detach', result });
  } catch (err) {
    if (isCredentialError(err)) return res.status(501).json({ error: 'AWS credentials not configured' });
    next(err);
  }
});

module.exports = router;
