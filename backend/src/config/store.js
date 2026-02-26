// Simple in-memory store for academic project
// In production, use a real database like MongoDB or PostgreSQL

const users = new Map();
const awsCredentials = new Map(); // userId -> { accessKeyId, secretAccessKey, region, accountId }

module.exports = { users, awsCredentials };
