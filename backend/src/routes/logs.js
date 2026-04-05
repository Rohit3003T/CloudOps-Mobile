const express = require("express");
const router = express.Router();
const pool = require("../db/postgres");

router.get("/", async (req, res) => {

  try {

    const result = await pool.query(
      "SELECT * FROM api_logs ORDER BY created_at DESC LIMIT 1000"
    );

    res.setHeader(
      "Content-Disposition",
      "attachment; filename=logs.json"
    );

    res.json(result.rows);

  } catch (err) {

    res.status(500).json({ error: err.message });

  }

});

module.exports = router;