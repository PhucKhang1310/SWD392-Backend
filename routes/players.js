const express = require("express");
const router = express.Router();

/**
 * GET /api/players
 * Demo endpoint for mobile/tunnel testing. Returns JSON array.
 * Replace with real MongoDB model when you have a Player collection.
 */
router.get("/", (req, res) => {
  res.json([]);
});

module.exports = router;
