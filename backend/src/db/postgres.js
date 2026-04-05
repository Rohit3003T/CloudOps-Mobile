const { Pool } = require("pg");

const pool = new Pool({
  host: "localhost",
  port: 5432,
  user: "postgres",
  password: "Rt@8085*123",
  database: "cloudmonitor"
});

module.exports = pool;