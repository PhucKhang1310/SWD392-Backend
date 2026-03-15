const express = require("express");
const router = express.Router();
const { getHistory, clearHistory, chat } = require("../controllers/chatController");
const { authenticate } = require("../middleware/auth");

// Buyer interact with chat bot endpoints
router.get("/history", authenticate, getHistory);
router.post("/clear", authenticate, clearHistory);
router.post("/", authenticate, chat);

module.exports = router;
