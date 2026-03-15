const mongoose = require("mongoose");

const productSchema = new mongoose.Schema(
  {
    name: { type: String, required: true, trim: true },
    description: { type: String, default: "" },
    price: { type: Number, required: true, min: 0 },
    stock: { type: Number, required: true, default: 0, min: 0 },
    images: [{ type: String }],
    category: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Category",
      required: true,
    },
    brand: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "Brand",
      required: true,
    },
    specifications: { type: Map, of: String, default: {} },
    isActive: { type: Boolean, default: true },
    // Flash Sale: giá khuyến mãi và thời điểm kết thúc (optional)
    flashSalePrice: { type: Number, default: null },
    flashSaleEnd: { type: Date, default: null },
  },
  { timestamps: true },
);

module.exports = mongoose.model("Product", productSchema);
