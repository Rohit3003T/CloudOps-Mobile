const pool = require("../db/postgres");

module.exports = function dbLogger(req, res, next) {

  res.on("finish", async () => {
    try {

      await pool.query(
        "INSERT INTO api_logs(endpoint, method, status) VALUES ($1,$2,$3)",
        [req.originalUrl, req.method, res.statusCode]
      );

    } catch (err) {
      console.error("DB log error:", err.message);
    }
  });

  next();
};