const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { v4: uuidv4 } = require('uuid');
const { users } = require('../config/store');
const { authenticate } = require('../middleware/auth');

const router = express.Router();

// Register
router.post('/register', async (req, res) => {
  try {
    const { email, password, name } = req.body;

    if (!email || !password || !name) {
      return res.status(400).json({ error: 'Email, password, and name are required' });
    }

    if (password.length < 6) {
      return res.status(400).json({ error: 'Password must be at least 6 characters' });
    }

    // Check if user exists
    for (const [, user] of users) {
      if (user.email === email) {
        return res.status(409).json({ error: 'User already exists with this email' });
      }
    }

    const userId = uuidv4();
    const passwordHash = await bcrypt.hash(password, 10);

    users.set(userId, {
      id: userId,
      email,
      name,
      passwordHash,
      createdAt: new Date().toISOString(),
    });

    const token = jwt.sign({ userId, email }, process.env.JWT_SECRET, { expiresIn: '7d' });

    res.status(201).json({
      message: 'Account created successfully',
      token,
      user: { id: userId, email, name },
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Login
router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ error: 'Email and password are required' });
    }

    let foundUser = null;
    for (const [, user] of users) {
      if (user.email === email) {
        foundUser = user;
        break;
      }
    }

    if (!foundUser) {
      return res.status(401).json({ error: 'Invalid email or password' });
    }

    const valid = await bcrypt.compare(password, foundUser.passwordHash);
    if (!valid) {
      return res.status(401).json({ error: 'Invalid email or password' });
    }

    const token = jwt.sign(
      { userId: foundUser.id, email: foundUser.email },
      process.env.JWT_SECRET,
      { expiresIn: '7d' }
    );

    res.json({
      message: 'Login successful',
      token,
      user: { id: foundUser.id, email: foundUser.email, name: foundUser.name },
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Get profile
router.get('/profile', authenticate, (req, res) => {
  const user = users.get(req.userId);
  if (!user) return res.status(404).json({ error: 'User not found' });
  res.json({ id: user.id, email: user.email, name: user.name, createdAt: user.createdAt });
});

module.exports = router;
