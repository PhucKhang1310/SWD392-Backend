const express = require("express");
const router = express.Router();
const {
  getCart,
  addItem,
  updateItemQuantity,
  removeItem,
  clearCart,
} = require("../controllers/cartController");
const { authenticate } = require("../middleware/auth");

router.get("/:userId", authenticate, getCart);
router.post("/add", authenticate, addItem);
router.put("/item/:itemId", authenticate, updateItemQuantity);
router.delete("/item/:itemId", authenticate, removeItem);
router.delete("/:userId/clear", authenticate, clearCart);

module.exports = router;
