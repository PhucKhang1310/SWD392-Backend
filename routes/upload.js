const express = require("express");
const router = express.Router();
const multer = require("multer");
const path = require("path");
const fs = require("fs");
const { authenticate, authorize } = require("../middleware/auth");

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const uploadDir = path.join(__dirname, "../uploads");
    if (!fs.existsSync(uploadDir)) {
      fs.mkdirSync(uploadDir, { recursive: true });
    }
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    const unique = Date.now() + "-" + Math.round(Math.random() * 1e9);
    const ext = path.extname(file.originalname) || ".jpg";
    cb(null, unique + ext);
  },
});

const upload = multer({
  storage,
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter: (req, file, cb) => {
    const allowed = /image\/(jpeg|jpg|png|gif|webp)/.test(file.mimetype);
    if (allowed) cb(null, true);
    else cb(new Error("Chỉ chấp nhận ảnh (jpg, png, gif, webp)"));
  },
});

router.post(
  "/upload",
  authenticate,
  authorize("admin", "staff"),
  (req, res) => {
    const multerUpload = upload.single("file");
    multerUpload(req, res, (err) => {
      if (err) {
        console.error("Multer upload error:", err);
        return res.status(400).json({ message: err.message || "Upload failed", details: err });
      }
      if (!req.file) {
        console.error("No file in req.file. Body was:", req.body);
        return res.status(400).json({ message: "Chưa chọn file" });
      }
      const baseUrl =
        process.env.API_URL || `http://localhost:${process.env.PORT || 5000}`;
      const url = `${baseUrl}/uploads/${req.file.filename}`;
      res.json({ url, filename: req.file.filename });
    });
  }
);

module.exports = router;
