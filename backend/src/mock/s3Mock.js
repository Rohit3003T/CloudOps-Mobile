'use strict';

const BUCKETS = [
  { name: 'my-app-assets-prod',    creationDate: new Date(Date.now() - 86400000 * 120).toISOString() },
  { name: 'my-app-backups-2024',   creationDate: new Date(Date.now() - 86400000 * 90).toISOString()  },
  { name: 'my-app-logs-archive',   creationDate: new Date(Date.now() - 86400000 * 60).toISOString()  },
  { name: 'my-terraform-state',    creationDate: new Date(Date.now() - 86400000 * 180).toISOString() },
];

const MOCK_FILES = [
  { key: 'index.html',          size: 2048,   lastModified: new Date(Date.now() - 3600000).toISOString(),   etag: 'abc123', storageClass: 'STANDARD' },
  { key: 'assets/logo.png',     size: 45231,  lastModified: new Date(Date.now() - 86400000).toISOString(),  etag: 'def456', storageClass: 'STANDARD' },
  { key: 'assets/styles.css',   size: 12048,  lastModified: new Date(Date.now() - 86400000 * 2).toISOString(), etag: 'ghi789', storageClass: 'STANDARD' },
  { key: 'uploads/report.pdf',  size: 512000, lastModified: new Date(Date.now() - 86400000 * 3).toISOString(), etag: 'jkl012', storageClass: 'STANDARD_IA' },
];

module.exports = {
  listBuckets: () => ({ success: true, mock: true, count: BUCKETS.length, owner: 'demo-account', buckets: BUCKETS }),
  listFiles:   (bucket) => ({ success: true, mock: true, bucket, prefix: '', count: MOCK_FILES.length, isTruncated: false, nextContinuationToken: null, files: MOCK_FILES }),
};
