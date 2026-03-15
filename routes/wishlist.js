const express = require("express");
const router = express.Router();
const { getWishlist, addProduct, removeProduct } = require("../controllers/wishlistController");
const { authenticate } = require("../middleware/auth");

router.get("/:userId", authenticate, getWishlist);
router.post("/add", authenticate, addProduct);
router.delete("/remove", authenticate, removeProduct);

module.exports = router;
