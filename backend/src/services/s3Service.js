const {
  ListBucketsCommand,
  CreateBucketCommand,
  DeleteBucketCommand,
  ListObjectsV2Command,
  PutObjectCommand,
  DeleteObjectCommand,
  HeadObjectCommand,
  GetObjectCommand,
} = require('@aws-sdk/client-s3');
const { getSignedUrl } = require('@aws-sdk/s3-request-presigner');
const { s3Client } = require('../aws/clients');

// ── Buckets ────────────────────────────────────────────────────────────────────

async function listBuckets() {
  const resp = await s3Client.send(new ListBucketsCommand({}));
  return {
    owner: resp.Owner?.DisplayName || null,
    buckets: (resp.Buckets || []).map(b => ({
      name: b.Name,
      creationDate: b.CreationDate,
    })),
  };
}

async function createBucket(bucketName, region) {
  const params = { Bucket: bucketName };
  const r = region || process.env.AWS_REGION || 'us-east-1';
  // us-east-1 must NOT include LocationConstraint — AWS quirk
  if (r !== 'us-east-1') {
    params.CreateBucketConfiguration = { LocationConstraint: r };
  }
  await s3Client.send(new CreateBucketCommand(params));
  return { bucket: bucketName, region: r, created: true };
}

async function deleteBucket(bucketName) {
  await s3Client.send(new DeleteBucketCommand({ Bucket: bucketName }));
  return { bucket: bucketName, deleted: true };
}

// ── Objects ────────────────────────────────────────────────────────────────────

async function listFiles(bucketName, prefix = '', maxKeys = 100) {
  const resp = await s3Client.send(new ListObjectsV2Command({
    Bucket: bucketName,
    Prefix: prefix,
    MaxKeys: maxKeys,
  }));
  return {
    bucket: bucketName,
    prefix,
    count: resp.KeyCount || 0,
    isTruncated: resp.IsTruncated || false,
    nextContinuationToken: resp.NextContinuationToken || null,
    files: (resp.Contents || []).map(obj => ({
      key:          obj.Key,
      size:         obj.Size,
      lastModified: obj.LastModified,
      etag:         obj.ETag?.replace(/"/g, ''),
      storageClass: obj.StorageClass,
    })),
  };
}

/**
 * Upload a file buffer to S3.
 * @param {string} bucketName
 * @param {string} key         — S3 object key (path)
 * @param {Buffer} body        — file buffer
 * @param {string} contentType — MIME type
 */
async function uploadFile(bucketName, key, body, contentType) {
  await s3Client.send(new PutObjectCommand({
    Bucket:      bucketName,
    Key:         key,
    Body:        body,
    ContentType: contentType || 'application/octet-stream',
  }));
  return { bucket: bucketName, key, uploaded: true, size: body.length };
}

async function deleteFile(bucketName, key) {
  await s3Client.send(new DeleteObjectCommand({ Bucket: bucketName, Key: key }));
  return { bucket: bucketName, key, deleted: true };
}

/** Generate a 1-hour presigned download URL */
async function getPresignedUrl(bucketName, key, expiresIn = 3600) {
  const command = new GetObjectCommand({ Bucket: bucketName, Key: key });
  const url = await getSignedUrl(s3Client, command, { expiresIn });
  return { bucket: bucketName, key, url, expiresIn };
}

module.exports = {
  listBuckets,
  createBucket,
  deleteBucket,
  listFiles,
  uploadFile,
  deleteFile,
  getPresignedUrl,
};
