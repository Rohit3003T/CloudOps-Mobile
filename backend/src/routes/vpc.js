'use strict';
const express = require('express');
const svc  = require('../services/vpcService');
const { isCredentialError } = require('../aws/clients');
const MOCK = require('../mock/vpcMock');

const router = express.Router();

// ── List VPCs ──────────────────────────────────────────────────────────────────
router.get('/', async (req, res, next) => {
  try {
    const vpcs = await svc.listVpcs();
    res.json({ success: true, count: vpcs.length, vpcs });
  } catch (err) {
    if (isCredentialError(err)) return res.json(MOCK.listVpcs());
    next(err);
  }
});

// ── List subnets (optionally filter by VPC) ────────────────────────────────────
router.get('/subnets', async (req, res, next) => {
  try {
    const subnets = await svc.listSubnets(req.query.vpcId);
    res.json({ success: true, count: subnets.length, subnets });
  } catch (err) {
    if (isCredentialError(err)) return res.json(MOCK.listSubnets());
    next(err);
  }
});

// ── List security groups (optionally filter by VPC) ────────────────────────────
router.get('/security-groups', async (req, res, next) => {
  try {
    const groups = await svc.listSecurityGroups(req.query.vpcId);
    res.json({ success: true, count: groups.length, securityGroups: groups });
  } catch (err) {
    if (isCredentialError(err)) return res.json(MOCK.listSecurityGroups());
    next(err);
  }
});

module.exports = router;
