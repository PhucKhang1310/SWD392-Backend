const express = require("express");
const router = express.Router();
const {
  login,
  googleLogin,
  register,
  getMe,
  forgotPassword,
  resetPassword,
  getAllUsers,
  updateProfile,
  changePassword,
} = require("../controllers/authController");
const { authenticate, authorize } = require("../middleware/auth");

router.post("/login", login);
router.post("/google", googleLogin);
router.post("/register", register);
router.post("/forgot-password", forgotPassword);
router.post("/reset-password", resetPassword);
router.get("/me", authenticate, getMe);
router.put("/profile", authenticate, updateProfile);
router.put("/change-password", authenticate, changePassword);
router.get("/users", authenticate, authorize("admin", "staff"), getAllUsers);

module.exports = router;
