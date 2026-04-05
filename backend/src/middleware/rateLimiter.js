const rateLimit = require('express-rate-limit');

const WINDOW_MS  = parseInt(process.env.RATE_LIMIT_WINDOW_MS || '900000', 10); // 15 min
const MAX        = parseInt(process.env.RATE_LIMIT_MAX       || '100',    10);
const AUTH_MAX   = parseInt(process.env.AUTH_RATE_LIMIT_MAX  || '10',     10);

/** General API limiter — 100 req / 15 min per IP */
const apiLimiter = rateLimit({
  windowMs: WINDOW_MS,
  max: MAX,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    error: 'Too many requests',
    message: `Rate limit exceeded. Try again after ${WINDOW_MS / 60000} minutes.`,
  },
});

/** Strict limiter for login — 10 attempts / 15 min per IP */
const authLimiter = rateLimit({
  windowMs: WINDOW_MS,
  max: AUTH_MAX,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    error: 'Too many login attempts',
    message: 'Account temporarily locked. Try again later.',
  },
});

/** Destructive-action limiter (terminate/delete) — 20 req / 15 min */
const destructiveLimiter = rateLimit({
  windowMs: WINDOW_MS,
  max: 20,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    error: 'Too many destructive requests',
    message: 'Slow down — destructive action limit reached.',
  },
});

module.exports = { apiLimiter, authLimiter, destructiveLimiter };
