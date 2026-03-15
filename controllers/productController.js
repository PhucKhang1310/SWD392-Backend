const Product = require("../models/Product");
const Category = require("../models/Category");
const Brand = require("../models/Brand");
const Review = require("../models/Review");

// Build a map of productId -> { avgRating, reviewCount } from Review collection
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

const getOrCreateCategory = async (name) => {
  if (!name || !String(name).trim()) return null;
  const n = String(name).trim();
  let cat = await Category.findOne({ name: n });
  if (!cat) cat = await Category.create({ name: n });
  return cat._id;
};

const getOrCreateBrand = async (name) => {
  const n = (name || "TechShop").trim();
  let brand = await Brand.findOne({ name: n });
  if (!brand) brand = await Brand.create({ name: n });
  return brand._id;
};

// Derive 3-state status from isActive + stock
const deriveStatus = (isActive, stock) => {
  if (!isActive) return "discontinued"; // Ngừng bán
  if (stock <= 0) return "outofstock"; // Hết hàng
  return "active"; // Còn hàng
};

// Helper: product đang trong flash sale khi có flashSalePrice và flashSaleEnd > now
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
    id: p._id,
    name: p.name,
    description: p.description,
    price: displayPrice,
    originalPrice: flashActive ? p.price : undefined,
    stock: p.stock,
    stockQuantity: p.stock,
    category: p.category?.name || p.category,
    imgUrl: p.images?.[0] || null,
    image: p.images?.[0] || null,
    isActive: p.isActive,
    productStatus: deriveStatus(p.isActive, p.stock),
    avgRating: (stats && stats.avgRating) || 0,
    reviewCount: (stats && stats.reviewCount) || 0,
    discount: discount || undefined,
    flashSalePrice: p.flashSalePrice || undefined,
    flashSaleEnd: p.flashSaleEnd ? new Date(p.flashSaleEnd).toISOString() : undefined,
  };
};

// Home page: only show "còn hàng" (isActive=true AND stock>0). Query ?flashSale=true = chỉ sản phẩm đang flash sale
const list = async (req, res) => {
  try {
    const flashSaleOnly = req.query.flashSale === "true";
    const filter = { isActive: true, stock: { $gt: 0 } };
    if (flashSaleOnly) {
      filter.flashSalePrice = { $ne: null, $gt: 0 };
      filter.flashSaleEnd = { $gt: new Date() };
    }
    const products = await Product.find(filter)
      .populate("category", "name")
      .populate("brand", "name")
      .lean();

    const productIds = products.map((p) => p._id);
    const reviewStats = await getReviewStats(productIds);

    const list = products.map((p) => {
      const stats = reviewStats[p._id.toString()] || {
        avgRating: 0,
        reviewCount: 0,
      };
      return toProductDto(p, stats);
    });
    res.json(list);
  } catch (err) {
    console.error("Product list error:", err);
    res.status(500).json({ message: "Server error" });
  }
};

// Admin/Staff: show all products with full status info
const listAll = async (req, res) => {
  try {
    const products = await Product.find()
      .populate("category", "name")
      .populate("brand", "name")
      .lean();

    const productIds = products.map((p) => p._id);
    const reviewStats = await getReviewStats(productIds);

    const list = products.map((p) => {
      const stats = reviewStats[p._id.toString()] || {
        avgRating: 0,
        reviewCount: 0,
      };
      return toProductDto(p, stats);
    });
    res.json(list);
  } catch (err) {
    console.error("Product listAll error:", err);
    res.status(500).json({ message: "Server error" });
  }
};

const create = async (req, res) => {
  try {
    const {
      name,
      description,
      price,
      stockQuantity,
      stock,
      category,
      status,
      imgUrl,
    } = req.body;
    if (!name || price == null) {
      return res.status(400).json({ message: "Thiếu tên hoặc giá" });
    }
    const categoryName = category || "Khác";
    const categoryId = await getOrCreateCategory(categoryName);
    const brandId = await getOrCreateBrand("TechShop");
    const product = await Product.create({
      name,
      description: description || "",
      price: Number(price),
      stock: Number(stockQuantity ?? stock ?? 0),
      images: imgUrl ? [imgUrl] : [],
      category: categoryId,
      brand: brandId,
      isActive: status !== "INACTIVE" && status !== "inactive",
      flashSalePrice: req.body.flashSalePrice != null ? Number(req.body.flashSalePrice) : undefined,
      flashSaleEnd: req.body.flashSaleEnd || undefined,
    });
    res.status(201).json({
      id: product._id,
      name: product.name,
      price: product.price,
      stock: product.stock,
      imgUrl: product.images?.[0] || null,
    });
  } catch (err) {
    console.error("Product create error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

const update = async (req, res) => {
  try {
    const {
      id,
      name,
      description,
      price,
      stockQuantity,
      stock,
      category,
      status,
      imgUrl,
      flashSalePrice,
      flashSaleEnd,
    } = req.body;
    if (!id) return res.status(400).json({ message: "Thiếu id sản phẩm" });
    const product = await Product.findById(id);
    if (!product)
      return res.status(404).json({ message: "Không tìm thấy sản phẩm" });
    if (name != null) product.name = name;
    if (description != null) product.description = description;
    if (price != null) product.price = Number(price);
    if (stockQuantity != null || stock != null)
      product.stock = Number(stockQuantity ?? stock ?? product.stock);
    if (category != null) {
      product.category = await getOrCreateCategory(category);
    }
    if (imgUrl != null) product.images = [imgUrl];
    if (status != null)
      product.isActive = status !== "INACTIVE" && status !== "inactive";
    if (flashSalePrice !== undefined) {
      const v = flashSalePrice === "" || flashSalePrice == null ? null : Number(flashSalePrice);
      if (v != null) {
        if (!product.isActive || product.stock <= 0) {
          return res.status(400).json({
            message: "Chỉ sản phẩm đang bán và còn hàng mới được đưa vào Flash Sale. Sản phẩm ngừng bán hoặc hết hàng không thể Flash Sale.",
          });
        }
        product.flashSalePrice = v;
      } else {
        product.flashSalePrice = null;
        product.flashSaleEnd = null;
      }
    }
    if (flashSaleEnd !== undefined) {
      const endVal = flashSaleEnd && flashSaleEnd !== "" ? new Date(flashSaleEnd) : null;
      if (endVal) {
        if (!product.isActive || product.stock <= 0) {
          return res.status(400).json({
            message: "Chỉ sản phẩm đang bán và còn hàng mới được đưa vào Flash Sale. Sản phẩm ngừng bán hoặc hết hàng không thể Flash Sale.",
          });
        }
        product.flashSaleEnd = endVal;
      } else {
        product.flashSaleEnd = null;
        product.flashSalePrice = null;
      }
    }
    await product.save();
    res.json({
      id: product._id,
      name: product.name,
      price: product.price,
      stock: product.stock,
      imgUrl: product.images?.[0] || null,
    });
  } catch (err) {
    console.error("Product update error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

const remove = async (req, res) => {
  try {
    const { id } = req.params;
    const product = await Product.findById(id);
    if (!product)
      return res.status(404).json({ message: "Không tìm thấy sản phẩm" });
    product.isActive = false;
    await product.save();
    res.json({ message: "Đã ẩn sản phẩm" });
  } catch (err) {
    console.error("Product delete error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

module.exports = { list, listAll, create, update, remove };
