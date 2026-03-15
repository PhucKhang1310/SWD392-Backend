const Wishlist = require("../models/Wishlist");
const Product = require("../models/Product");
const Review = require("../models/Review");

const getReviewStats = async (productIds) => {
  const stats = await Review.aggregate([
    { $match: { productId: { $in: productIds } } },
    {
      $group: {
        _id: "$productId",
        avgRating: { $avg: "$rating" },
        reviewCount: { $sum: 1 },
      },
    },
  ]);
  const map = {};
  stats.forEach((s) => {
    map[s._id.toString()] = {
      avgRating: Math.round(s.avgRating * 10) / 10,
      reviewCount: s.reviewCount,
    };
  });
  return map;
};

const isFlashSaleActive = (p) =>
  p.flashSalePrice != null &&
  p.flashSaleEnd &&
  new Date(p.flashSaleEnd) > new Date();

const toProductDto = (p, stats) => {
  const flashActive = isFlashSaleActive(p);
  const displayPrice = flashActive ? p.flashSalePrice : p.price;
  const discount =
    flashActive && p.price > 0
      ? Math.round((1 - p.flashSalePrice / p.price) * 100)
      : 0;
  return {
    id: String(p._id),
    name: p.name,
    description: p.description || "",
    price: displayPrice,
    originalPrice: flashActive ? p.price : undefined,
    discount: discount || undefined,
    stock: p.stock,
    image: p.images?.[0] || "",
    category: p.category?.name || (p.category && String(p.category)) || "",
    avgRating: (stats && stats.avgRating) || 0,
    reviewCount: (stats && stats.reviewCount) || 0,
    flashSalePrice: p.flashSalePrice || undefined,
    flashSaleEnd: p.flashSaleEnd ? new Date(p.flashSaleEnd).toISOString() : undefined,
  };
};

/** GET /api/wishlist/:userId - Lấy danh sách sản phẩm yêu thích (đã lưu MongoDB Atlas) */
const getWishlist = async (req, res) => {
  try {
    const { userId } = req.params;
    if (!userId) {
      return res.status(400).json({ message: "Thiếu userId" });
    }
    if (String(req.user._id) !== String(userId)) {
      return res.status(403).json({ message: "Chỉ được xem wishlist của chính mình" });
    }
    let doc = await Wishlist.findOne({ userId }).lean();
    if (!doc || !doc.productIds || doc.productIds.length === 0) {
      return res.json({ products: [] });
    }
    const productIds = doc.productIds;
    const products = await Product.find({ _id: { $in: productIds } })
      .populate("category", "name")
      .lean();
    const reviewStats = await getReviewStats(productIds);
    const list = products.map((p) => {
      const stats = reviewStats[p._id.toString()] || { avgRating: 0, reviewCount: 0 };
      return toProductDto(p, stats);
    });
    res.json({ products: list });
  } catch (err) {
    console.error("Wishlist get error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

/** POST /api/wishlist/add - Thêm sản phẩm vào yêu thích */
const addProduct = async (req, res) => {
  try {
    const { userId, productId } = req.body;
    if (!userId || !productId) {
      return res.status(400).json({ message: "Thiếu userId hoặc productId" });
    }
    if (String(req.user._id) !== String(userId)) {
      return res.status(403).json({ message: "Chỉ được thêm vào wishlist của chính mình" });
    }
    const product = await Product.findById(productId);
    if (!product) {
      return res.status(404).json({ message: "Không tìm thấy sản phẩm" });
    }
    let doc = await Wishlist.findOne({ userId });
    if (!doc) {
      doc = await Wishlist.create({ userId, productIds: [] });
    }
    const idStr = String(productId);
    if (!doc.productIds.some((id) => String(id) === idStr)) {
      doc.productIds.push(productId);
      await doc.save();
    }
    const products = await Product.find({ _id: { $in: doc.productIds } })
      .populate("category", "name")
      .lean();
    const reviewStats = await getReviewStats(doc.productIds);
    const list = products.map((p) => {
      const stats = reviewStats[p._id.toString()] || { avgRating: 0, reviewCount: 0 };
      return toProductDto(p, stats);
    });
    res.json({ products: list });
  } catch (err) {
    console.error("Wishlist add error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

/** DELETE /api/wishlist/remove - Xóa sản phẩm khỏi yêu thích */
const removeProduct = async (req, res) => {
  try {
    const { userId, productId } = req.body;
    if (!userId || !productId) {
      return res.status(400).json({ message: "Thiếu userId hoặc productId" });
    }
    if (String(req.user._id) !== String(userId)) {
      return res.status(403).json({ message: "Chỉ được xóa khỏi wishlist của chính mình" });
    }
    const doc = await Wishlist.findOne({ userId });
    if (!doc) {
      return res.json({ products: [] });
    }
    doc.productIds = doc.productIds.filter((id) => String(id) !== String(productId));
    await doc.save();
    if (doc.productIds.length === 0) {
      return res.json({ products: [] });
    }
    const products = await Product.find({ _id: { $in: doc.productIds } })
      .populate("category", "name")
      .lean();
    const reviewStats = await getReviewStats(doc.productIds);
    const list = products.map((p) => {
      const stats = reviewStats[p._id.toString()] || { avgRating: 0, reviewCount: 0 };
      return toProductDto(p, stats);
    });
    res.json({ products: list });
  } catch (err) {
    console.error("Wishlist remove error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

module.exports = { getWishlist, addProduct, removeProduct };
