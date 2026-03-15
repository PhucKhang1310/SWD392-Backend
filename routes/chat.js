const express = require("express");
const router = express.Router();
const { getHistory, clearHistory, chat, pingDb } = require("../controllers/chatController");
const { authenticate } = require("../middleware/auth");

// Diagnostics (no auth — remove after confirming server DB access is healthy)
router.get("/ping-db", pingDb);

// Buyer interact with chat bot endpoints
router.get("/history", authenticate, getHistory);
router.post("/clear", authenticate, clearHistory);
router.post("/", authenticate, chat);

module.exports = router;
