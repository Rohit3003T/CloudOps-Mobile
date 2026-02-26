require('dotenv').config();
const express = require('express');
const cors = require('cors');

const authRoutes = require('./routes/auth');
const awsRoutes = require('./routes/aws');
const ec2Routes = require('./routes/ec2');
const s3Routes = require('./routes/s3');
const cloudwatchRoutes = require('./routes/cloudwatch');
const costRoutes = require('./routes/cost');
const securityRoutes = require('./routes/security');

const app = express();

app.use(cors());
app.use(express.json());

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'ok', service: 'CloudOps Mobile API', version: '1.0.0' });
});

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/aws', awsRoutes);
app.use('/api/ec2', ec2Routes);
app.use('/api/s3', s3Routes);
app.use('/api/cloudwatch', cloudwatchRoutes);
app.use('/api/cost', costRoutes);
app.use('/api/security', securityRoutes);

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Route not found' });
});

// Error handler
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ error: 'Internal server error', message: err.message });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 CloudOps Mobile API running on port ${PORT}`);
});
