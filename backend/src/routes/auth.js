const express = require('express');
const bcrypt = require('bcryptjs');
const { generateToken } = require('../middleware/auth');

const router = express.Router();

// In production, replace with a real database lookup
// For demo: credentials can be set via environment variables
const DEMO_USERNAME = process.env.DEMO_USERNAME || 'admin';
const DEMO_PASSWORD_HASH = bcrypt.hashSync(process.env.DEMO_PASSWORD || 'password123', 10);

/**
 * POST /api/login
 * Body: { username: string, password: string }
 * Returns: { token: string, user: { username, role } }
 */
router.post('/login', async (req, res) => {
  try {
    const { username, password } = req.body;

    if (!username || !password) {
      return res.status(400).json({ error: 'Username and password are required.' });
    }

    // Validate credentials
    if (username !== DEMO_USERNAME) {
      return res.status(401).json({ error: 'Invalid username or password.' });
    }

    const passwordMatch = await bcrypt.compare(password, DEMO_PASSWORD_HASH);
    if (!passwordMatch) {
      return res.status(401).json({ error: 'Invalid username or password.' });
    }

    // Generate JWT token
    const token = generateToken({
      username,
      role: 'admin',
      iat: Math.floor(Date.now() / 1000)
    });

    res.json({
      success: true,
      token,
      user: {
        username,
        role: 'admin'
      },
      expiresIn: '24h'
    });
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({ error: 'Authentication failed.' });
  }
});

module.exports = router;
