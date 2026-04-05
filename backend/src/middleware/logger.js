const morgan = require('morgan');

/**
 * Custom morgan token: authenticated username (from JWT middleware)
 */
morgan.token('user', (req) => req.user?.username || 'anonymous');

/**
 * Compact log format:
 *   POST /api/login 200 42ms — anonymous
 */
const requestLogger = morgan(
  ':method :url :status :response-time ms — :user',
  {
    skip: (req) => req.url === '/health', // skip noisy health-check polls
  }
);

module.exports = { requestLogger };
