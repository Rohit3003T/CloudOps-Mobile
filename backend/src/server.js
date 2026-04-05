'use strict';
require('dotenv').config();

const express = require('express');
const cors    = require('cors');

const { requestLogger }                          = require('./middleware/logger');
const { apiLimiter, authLimiter }                = require('./middleware/rateLimiter');
const { authenticateToken }                      = require('./middleware/auth');
const { hasAwsCredentials, ec2Client, s3Client,
        lambdaClient, cwClient, costClient }      = require('./aws/clients');

const authRoutes       = require('./routes/auth');
const ec2Routes        = require('./routes/ec2');
const s3Routes         = require('./routes/s3');
const lambdaRoutes     = require('./routes/lambda');
const costRoutes       = require('./routes/cost');
const ebsRoutes        = require('./routes/ebs');
const vpcRoutes        = require('./routes/vpc');
const monitoringRoutes = require('./routes/monitoring');
const cicdRoutes       = require('./routes/cicd');
const dbLogger = require("./middleware/dbLogger");
const logsRoute = require("./routes/logs");
const app  = express();
const PORT = process.env.PORT || 3000;

// ── Core middleware ────────────────────────────────────────────────────────────
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(requestLogger);
app.use(dbLogger);

app.use("/api/logs", logsRoute);

// ── Health & diagnostics (public, no rate limit) ───────────────────────────────

app.get('/health', (req, res) => {
  const uptime = process.uptime();
  res.json({
    status:    'OK',
    version:   '2.0.0',
    timestamp: new Date().toISOString(),
    uptime:    `${Math.floor(uptime / 3600)}h ${Math.floor((uptime % 3600) / 60)}m ${Math.floor(uptime % 60)}s`,
    node:      process.version,
    env:       process.env.NODE_ENV || 'development',
    aws: {
      credentialsConfigured: hasAwsCredentials(),
      region: process.env.AWS_REGION || 'us-east-1',
    },
    github: {
      tokenConfigured: !!process.env.GITHUB_TOKEN,
    },
  });
});

/** AWS connectivity test — calls DescribeRegions (cheap, read-only) */
app.get('/health/aws', authenticateToken, async (req, res) => {
  const { DescribeRegionsCommand } = require('@aws-sdk/client-ec2');
  const checks = {};
  const start  = Date.now();

  const run = async (name, fn) => {
    try {
      await fn();
      checks[name] = { status: 'ok' };
    } catch (err) {
      checks[name] = { status: 'error', error: err.message };
    }
  };

  await Promise.allSettled([
    run('ec2',       () => ec2Client.send(new DescribeRegionsCommand({ AllRegions: false }))),
    run('s3',        () => s3Client.send(new (require('@aws-sdk/client-s3').ListBucketsCommand)({}))),
    run('lambda',    () => lambdaClient.send(new (require('@aws-sdk/client-lambda').ListFunctionsCommand)({ MaxItems: 1 }))),
    run('cloudwatch',() => cwClient.send(new (require('@aws-sdk/client-cloudwatch').ListMetricsCommand)({ Namespace: 'AWS/EC2' }))),
  ]);

  const allOk = Object.values(checks).every(c => c.status === 'ok');
  res.status(allOk ? 200 : 207).json({
    success:      allOk,
    durationMs:   Date.now() - start,
    region:       process.env.AWS_REGION || 'us-east-1',
    services:     checks,
  });
});

// ── Auth (rate-limited) ────────────────────────────────────────────────────────
app.use('/api/auth',  authLimiter, authRoutes);
// Legacy /api/login compatibility
app.use('/api',       authLimiter, authRoutes);

// ── Apply global API rate limiter to all /api/* routes ─────────────────────────
app.use('/api', apiLimiter);

// ── Protected routes ───────────────────────────────────────────────────────────
app.use('/api/ec2',        authenticateToken, ec2Routes);
app.use('/api/s3',         authenticateToken, s3Routes);
app.use('/api/lambda',     authenticateToken, lambdaRoutes);
app.use('/api/cost',       authenticateToken, costRoutes);
app.use('/api/ebs',        authenticateToken, ebsRoutes);
app.use('/api/vpc',        authenticateToken, vpcRoutes);
app.use('/api/monitoring', authenticateToken, monitoringRoutes);
app.use('/api/cicd',       authenticateToken, cicdRoutes);

// ── 404 handler ────────────────────────────────────────────────────────────────
app.use((req, res) => {
  res.status(404).json({ error: 'Not found', path: req.originalUrl });
});

// ── Global error handler ───────────────────────────────────────────────────────
app.use((err, req, res, next) => { // eslint-disable-line no-unused-vars
  console.error(`[ERROR] ${req.method} ${req.originalUrl} —`, err.message);
  const status = err.statusCode || err.status || 500;
  res.status(status).json({
    error:   err.message || 'Internal server error',
    ...(process.env.NODE_ENV !== 'production' && { stack: err.stack }),
  });
});

app.listen(PORT, () => {
  console.log('');
  console.log('╔══════════════════════════════════════════════════╗');
  console.log('║      ☁  Cloud Monitor Backend v2.0.0  ☁          ║');
  console.log('╚══════════════════════════════════════════════════╝');
  console.log(`  Port    : ${PORT}`);
  console.log(`  Env     : ${process.env.NODE_ENV || 'development'}`);
  console.log(`  AWS     : ${hasAwsCredentials() ? '✅ credentials configured' : '⚠️  no credentials (mock mode)'}`);
  console.log(`  GitHub  : ${process.env.GITHUB_TOKEN ? '✅ token configured' : '⚠️  no token'}`);
  console.log('');
  console.log('  Endpoints:');
  console.log(`  GET  /health`);
  console.log(`  GET  /health/aws         (auth required)`);
  console.log(`  POST /api/login`);
  console.log(`  *    /api/ec2            /api/s3       /api/lambda`);
  console.log(`  *    /api/cost           /api/ebs      /api/vpc`);
  console.log(`  *    /api/monitoring     /api/cicd`);
  console.log('');
});

module.exports = app;
