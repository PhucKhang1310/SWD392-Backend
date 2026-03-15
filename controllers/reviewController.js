const Review = require("../models/Review");
const Order = require("../models/Order");

// POST /api/reviews
// Buyer tạo đánh giá cho sản phẩm trong đơn hàng đã hoàn thành
const createReview = async (req, res) => {
  try {
    const { productId, orderId, rating, comment } = req.body;
    const userId = req.user._id;

    if (!productId || !orderId || !rating) {
      return res.status(400).json({ message: "Thiếu thông tin đánh giá" });
    }
    if (rating < 1 || rating > 5) {
      return res.status(400).json({ message: "Số sao phải từ 1 đến 5" });
    }

    // Kiểm tra đơn hàng tồn tại, thuộc về user, và đã hoàn thành
    const order = await Order.findById(orderId);
    if (!order) {
      return res.status(404).json({ message: "Đơn hàng không tồn tại" });
    }
    if (order.userId.toString() !== userId.toString()) {
      return res
        .status(403)
        .json({ message: "Bạn không có quyền đánh giá đơn hàng này" });
    }
    if (order.status !== "completed") {
      return res
        .status(400)
        .json({ message: "Chỉ có thể đánh giá đơn hàng đã hoàn thành" });
    }

    // Kiểm tra sản phẩm có trong đơn hàng không
    const itemInOrder = order.items.find(
      (item) => item.productId.toString() === productId,
    );
    if (!itemInOrder) {
      return res
        .status(400)
        .json({ message: "Sản phẩm không có trong đơn hàng này" });
    }

    // Kiểm tra đã đánh giá chưa
    const existing = await Review.findOne({ userId, productId, orderId });
    if (existing) {
      return res
        .status(409)
        .json({ message: "Bạn đã đánh giá sản phẩm này rồi" });
    }

    const review = await Review.create({
      userId,
      productId,
      orderId,
      rating: Number(rating),
      comment: comment?.trim() || "",
      userName: req.user.name || "",
    });

    res.status(201).json({
      message: "Đánh giá thành công",
      review: {
        id: review._id,
        rating: review.rating,
        comment: review.comment,
        userName: review.userName,
        createdAt: review.createdAt,
      },
    });
  } catch (err) {
    if (err.code === 11000) {
      return res
        .status(409)
        .json({ message: "Bạn đã đánh giá sản phẩm này rồi" });
    }
    console.error("Create review error:", err);
    res.status(500).json({ message: "Server error" });
  }
};

// GET /api/reviews/product/:productId
// Lấy tất cả đánh giá của một sản phẩm
const getProductReviews = async (req, res) => {
  try {
    const { productId } = req.params;
    const reviews = await Review.find({ productId })
      .sort({ createdAt: -1 })
      .lean();

    const result = reviews.map((r) => ({
      id: r._id,
      rating: r.rating,
      comment: r.comment,
      userName: r.userName,
      createdAt: r.createdAt,
    }));

    // Tính trung bình sao
    const avgRating =
      result.length > 0
        ? result.reduce((sum, r) => sum + r.rating, 0) / result.length
        : 0;

    res.json({
      reviews: result,
      total: result.length,
      avgRating: Math.round(avgRating * 10) / 10,
    });
  } catch (err) {
    console.error("Get product reviews error:", err);
    res.status(500).json({ message: "Server error" });
  }
};

// GET /api/reviews/check?productId=&orderId=
// Kiểm tra user đã review sản phẩm trong đơn hàng chưa
const checkReviewed = async (req, res) => {
  try {
    const { productId, orderId } = req.query;
    const userId = req.user._id;
    const review = await Review.findOne({ userId, productId, orderId });
    res.json({ reviewed: !!review, review: review || null });
  } catch (err) {
    console.error("Check reviewed error:", err);
    res.status(500).json({ message: "Server error" });
  }
};

module.exports = { createReview, getProductReviews, checkReviewed };
