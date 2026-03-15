const express = require("express");
const router = express.Router();
const {
  createOrder,
  getOrdersByUserId,
  getAllOrders,
  getOrderById,
  updateOrderStatus,
  deleteOrder,
  cancelOrder,
  checkoutOrder,
  momoCallback,
} = require("../controllers/orderController");
const { authenticate, authorize } = require("../middleware/auth");

// Buyer tạo đơn hàng
router.post("/", authenticate, createOrder);

// Buyer xem đơn hàng của mình
router.get("/user/:userId", authenticate, getOrdersByUserId);

// Admin/Staff xem tất cả đơn hàng
router.get("/", authenticate, authorize("admin", "staff"), getAllOrders);

// Xem chi tiết đơn hàng
router.get("/:orderId", authenticate, getOrderById);

// Admin/Staff cập nhật trạng thái
router.put(
  "/:orderId/status",
  authenticate,
  authorize("admin", "staff"),
  updateOrderStatus,
);

// Buyer hủy đơn hàng (chỉ khi pending)
router.put("/:orderId/cancel", authenticate, cancelOrder);

// Buyer thanh toán đơn hàng bằng Momo
router.post("/:orderId/checkout", authenticate, checkoutOrder);

// Momo Callback Webhook (Server-to-Server, so usually unauthenticated but expects signature validation in prod)
router.post("/momo/callback", momoCallback);

// Admin xóa đơn hàng
router.delete("/:orderId", authenticate, authorize("admin"), deleteOrder);

module.exports = router;
