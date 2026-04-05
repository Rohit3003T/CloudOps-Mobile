'use strict';
const express = require('express');
const multer  = require('multer');
const svc     = require('../services/s3Service');
const { isCredentialError } = require('../aws/clients');
const { destructiveLimiter } = require('../middleware/rateLimiter');
const MOCK = require('../mock/s3Mock');

const router  = express.Router();
const upload  = multer({
  storage: multer.memoryStorage(),
  limits:  { fileSize: parseInt(process.env.S3_MAX_UPLOAD_BYTES || '10485760', 10) },
});

// ── List buckets ───────────────────────────────────────────────────────────────
router.get('/', async (req, res, next) => {
  try {
    const data = await svc.listBuckets();
    res.json({ success: true, count: data.buckets.length, ...data });
  } catch (err) {
    if (isCredentialError(err)) return res.json(MOCK.listBuckets());
    next(err);
  }
});

// ── Create bucket ──────────────────────────────────────────────────────────────
router.post('/', async (req, res, next) => {
  const { bucketName, region } = req.body;
  if (!bucketName) return res.status(400).json({ error: 'bucketName is required' });
  try {
    const result = await svc.createBucket(bucketName, region);
    res.status(201).json({ success: true, ...result });
  } catch (err) {
    if (isCredentialError(err)) return res.status(501).json({ error: 'AWS credentials not configured' });
    next(err);
  }
});

// ── Delete bucket ──────────────────────────────────────────────────────────────
router.delete('/:bucketName', destructiveLimiter, async (req, res, next) => {
  try {
    const result = await svc.deleteBucket(req.params.bucketName);
    res.json({ success: true, ...result });
  } catch (err) {
    if (isCredentialError(err)) return res.status(501).json({ error: 'AWS credentials not configured' });
    next(err);
  }
});

// ── List files in bucket ───────────────────────────────────────────────────────
router.get('/:bucketName/files', async (req, res, next) => {
  try {
    const { prefix = '', maxKeys = 100 } = req.query;
    const data = await svc.listFiles(req.params.bucketName, prefix, parseInt(maxKeys, 10));
    res.json({ success: true, ...data });
  } catch (err) {
    if (isCredentialError(err)) return res.json(MOCK.listFiles(req.params.bucketName));
    next(err);
  }
});

// ── Upload file to bucket ──────────────────────────────────────────────────────
router.post('/:bucketName/files', upload.single('file'), async (req, res, next) => {
  if (!req.file) return res.status(400).json({ error: 'No file uploaded. Use multipart/form-data with field "file".' });
  const key = req.body.key || req.file.originalname;
  try {
    const result = await svc.uploadFile(
      req.params.bucketName, key, req.file.buffer, req.file.mimetype
    );
    res.status(201).json({ success: true, ...result });
  } catch (err) {
    if (isCredentialError(err)) return res.status(501).json({ error: 'AWS credentials not configured' });
    next(err);
  }
});

// ── Delete file ────────────────────────────────────────────────────────────────
router.delete('/:bucketName/files/:key(*)', destructiveLimiter, async (req, res, next) => {
  try {
    const result = await svc.deleteFile(req.params.bucketName, req.params.key);
    res.json({ success: true, ...result });
  } catch (err) {
    if (isCredentialError(err)) return res.status(501).json({ error: 'AWS credentials not configured' });
    next(err);
  }
});

// ── Get presigned download URL ─────────────────────────────────────────────────
router.get('/:bucketName/files/:key(*)/url', async (req, res, next) => {
  const expiresIn = parseInt(req.query.expiresIn || '3600', 10);
  try {
    const result = await svc.getPresignedUrl(req.params.bucketName, req.params.key, expiresIn);
    res.json({ success: true, ...result });
  } catch (err) {
    if (isCredentialError(err)) return res.status(501).json({ error: 'AWS credentials not configured' });
    next(err);
  }
});

module.exports = router;
