const Cart = require("../models/Cart");
const Product = require("../models/Product");

/** Thêm imageUrl vào từng cart item từ Product.images[0] */
const withProductImages = async (items) => {
  if (!items?.length) return items;
  const ids = items.map((i) => i.productId).filter(Boolean);
  const products = await Product.find({ _id: { $in: ids } }).select("images").lean();
  const imageMap = {};
  products.forEach((p) => {
    imageMap[p._id.toString()] = p.images?.[0] || null;
  });
  return items.map((item) => ({
    ...item,
    imageUrl: imageMap[item.productId] || null,
  }));
};

const getCart = async (req, res) => {
  try {
    const { userId } = req.params;
    let cart = await Cart.findOne({ userId });
    if (!cart) {
      cart = await Cart.create({ userId, items: [] });
    }
    let cartItems = (cart.items || []).map((item) => ({
      id: String(item._id),
      productId: String(item.productId),
      productName: item.productName || "",
      priceAtTime: item.priceAtTime,
      quantity: item.quantity,
    }));
    cartItems = await withProductImages(cartItems);
    res.json({ cartItems });
  } catch (err) {
    console.error("Cart get error:", err);
    res.status(500).json({ message: "Server error" });
  }
};

const addItem = async (req, res) => {
  try {
    const { userId, productId, quantity } = req.body;
    if (!userId || !productId) {
      return res.status(400).json({ message: "Thiếu userId hoặc productId" });
    }
    const product = await Product.findById(productId);
    if (!product) return res.status(404).json({ message: "Không tìm thấy sản phẩm" });
    const now = new Date();
    const inFlashSale =
      product.flashSalePrice != null &&
      product.flashSaleEnd &&
      new Date(product.flashSaleEnd) > now;
    const priceAtTime = inFlashSale ? product.flashSalePrice : product.price;
    const qty = Math.max(1, Number(quantity) || 1);
    let cart = await Cart.findOne({ userId });
    if (!cart) cart = await Cart.create({ userId, items: [] });
    const existing = cart.items.find(
      (i) => String(i.productId) === String(productId)
    );
    if (existing) {
      existing.quantity += qty;
      existing.productName = product.name;
      existing.priceAtTime = priceAtTime;
    } else {
      cart.items.push({
        productId,
        productName: product.name,
        priceAtTime,
        quantity: qty,
      });
    }
    await cart.save();
    let cartItems = cart.items.map((item) => ({
      id: String(item._id),
      productId: String(item.productId),
      productName: item.productName,
      priceAtTime: item.priceAtTime,
      quantity: item.quantity,
    }));
    cartItems = await withProductImages(cartItems);
    res.json({ cartItems });
  } catch (err) {
    console.error("Cart add error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

const updateItemQuantity = async (req, res) => {
  try {
    const { itemId } = req.params;
    const quantity = parseInt(req.query.quantity, 10);
    if (isNaN(quantity) || quantity < 1) {
      return res.status(400).json({ message: "Số lượng không hợp lệ" });
    }
    const cart = await Cart.findOne({ "items._id": itemId });
    if (!cart) return res.status(404).json({ message: "Không tìm thấy mục giỏ hàng" });
    const item = cart.items.id(itemId);
    if (!item) return res.status(404).json({ message: "Không tìm thấy mục giỏ hàng" });
    item.quantity = quantity;
    await cart.save();
    let cartItems = cart.items.map((i) => ({
      id: String(i._id),
      productId: String(i.productId),
      productName: i.productName,
      priceAtTime: i.priceAtTime,
      quantity: i.quantity,
    }));
    cartItems = await withProductImages(cartItems);
    res.json({ cartItems });
  } catch (err) {
    console.error("Cart update quantity error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

const removeItem = async (req, res) => {
  try {
    const { itemId } = req.params;
    const cart = await Cart.findOne({ "items._id": itemId });
    if (!cart) return res.status(404).json({ message: "Không tìm thấy mục giỏ hàng" });
    cart.items.pull(itemId);
    await cart.save();
    let cartItems = cart.items.map((i) => ({
      id: String(i._id),
      productId: String(i.productId),
      productName: i.productName,
      priceAtTime: i.priceAtTime,
      quantity: i.quantity,
    }));
    cartItems = await withProductImages(cartItems);
    res.json({ cartItems });
  } catch (err) {
    console.error("Cart remove item error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

const clearCart = async (req, res) => {
  try {
    const { userId } = req.params;
    const cart = await Cart.findOne({ userId });
    if (cart) {
      cart.items = [];
      await cart.save();
    }
    res.json({ cartItems: [] });
  } catch (err) {
    console.error("Cart clear error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

module.exports = { getCart, addItem, updateItemQuantity, removeItem, clearCart };
