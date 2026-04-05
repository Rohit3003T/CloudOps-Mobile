'use strict';
const express = require('express');
const svc  = require('../services/lambdaService');
const { isCredentialError } = require('../aws/clients');
const MOCK = require('../mock/lambdaMock');

const router = express.Router();

// ── List functions ─────────────────────────────────────────────────────────────
router.get('/', async (req, res, next) => {
  try {
    const functions = await svc.listFunctions(parseInt(req.query.maxItems || '50', 10));
    res.json({ success: true, count: functions.length, functions });
  } catch (err) {
    if (isCredentialError(err)) return res.json(MOCK.listFunctions());
    next(err);
  }
});

// ── Function details ───────────────────────────────────────────────────────────
router.get('/:functionName', async (req, res, next) => {
  try {
    const fn = await svc.getFunctionDetails(req.params.functionName);
    res.json({ success: true, function: fn });
  } catch (err) {
    if (err.name === 'ResourceNotFoundException')
      return res.status(404).json({ error: 'Function not found' });
    if (isCredentialError(err)) return res.json(MOCK.functionDetails(req.params.functionName));
    next(err);
  }
});

module.exports = router;
