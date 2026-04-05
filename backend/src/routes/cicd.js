'use strict';
const express = require('express');
const svc = require('../services/githubService');

const router = express.Router();

function resolveOwnerRepo(query, body = {}) {
  const owner = query.owner || body.owner || process.env.GITHUB_DEFAULT_OWNER;
  const repo  = query.repo  || body.repo  || process.env.GITHUB_DEFAULT_REPO;
  if (!owner || !repo) {
    const err = new Error('owner and repo are required (or set GITHUB_DEFAULT_OWNER / GITHUB_DEFAULT_REPO in .env)');
    err.statusCode = 400;
    throw err;
  }
  return { owner, repo };
}

// ── List workflow runs ─────────────────────────────────────────────────────────
// GET /api/cicd/runs?owner=acme&repo=myapp&branch=main&perPage=10
router.get('/runs', async (req, res, next) => {
  try {
    const { owner, repo } = resolveOwnerRepo(req.query);
    const { branch, status, perPage = 10 } = req.query;
    const data = await svc.listWorkflowRuns(owner, repo, { branch, status, perPage: parseInt(perPage, 10) });
    res.json({ success: true, owner, repo, ...data });
  } catch (err) {
    if (err.statusCode) return res.status(err.statusCode).json({ error: err.message });
    next(err);
  }
});

// ── Single run details ─────────────────────────────────────────────────────────
router.get('/runs/:runId', async (req, res, next) => {
  try {
    const { owner, repo } = resolveOwnerRepo(req.query);
    const run = await svc.getWorkflowRun(owner, repo, req.params.runId);
    res.json({ success: true, run });
  } catch (err) {
    if (err.statusCode) return res.status(err.statusCode).json({ error: err.message });
    next(err);
  }
});

// ── Jobs for a run ─────────────────────────────────────────────────────────────
router.get('/runs/:runId/jobs', async (req, res, next) => {
  try {
    const { owner, repo } = resolveOwnerRepo(req.query);
    const data = await svc.getRunJobs(owner, repo, req.params.runId);
    res.json({ success: true, runId: req.params.runId, ...data });
  } catch (err) {
    if (err.statusCode) return res.status(err.statusCode).json({ error: err.message });
    next(err);
  }
});

// ── List workflows ─────────────────────────────────────────────────────────────
router.get('/workflows', async (req, res, next) => {
  try {
    const { owner, repo } = resolveOwnerRepo(req.query);
    const data = await svc.listWorkflows(owner, repo);
    res.json({ success: true, owner, repo, ...data });
  } catch (err) {
    if (err.statusCode) return res.status(err.statusCode).json({ error: err.message });
    next(err);
  }
});

module.exports = router;
