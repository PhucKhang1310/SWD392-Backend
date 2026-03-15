const express = require("express");
const router = express.Router();
const {
  createReview,
  getProductReviews,
  checkReviewed,
} = require("../controllers/reviewController");
const { authenticate } = require("../middleware/auth");

// POST /api/reviews - tạo đánh giá (phải đăng nhập)
router.post("/", authenticate, createReview);

// GET /api/reviews/product/:productId - lấy reviews của sản phẩm (public)
router.get("/product/:productId", getProductReviews);

// GET /api/reviews/check - kiểm tra đã review chưa (phải đăng nhập)
router.get("/check", authenticate, checkReviewed);

module.exports = router;
