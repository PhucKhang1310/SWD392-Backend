const Category = require("../models/Category");

// GET /api/categories - danh sách danh mục (active), format giống FE cần: { id, name }
const getList = async (req, res) => {
  try {
    const categories = await Category.find({ isActive: true })
      .select("name")
      .lean();
    const list = categories.map((c) => ({
      id: c._id.toString(),
      name: c.name,
    }));
    res.json(list);
  } catch (err) {
    res.status(500).json({ message: err.message || "Lỗi khi lấy danh mục" });
  }
};

module.exports = { getList };
