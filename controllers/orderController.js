const Order = require("../models/Order");
const Cart = require("../models/Cart");
const crypto = require("crypto");
const https = require("https");

// POST /api/orders
// Buyer tạo đơn hàng từ giỏ hàng
const createOrder = async (req, res) => {
  try {
    const {
      userId,
      shippingName,
      shippingPhone,
      shippingAddress,
      paymentMethod,
      note,
    } = req.body;

    if (!userId) {
      return res.status(400).json({ message: "Thiếu userId" });
    }

    // Lấy giỏ hàng của user
    const cart = await Cart.findOne({ userId });
    if (!cart || cart.items.length === 0) {
      return res
        .status(400)
        .json({ message: "Giỏ hàng trống, không thể tạo đơn hàng" });
    }

    // Tính tổng tiền
    const totalAmount = cart.items.reduce(
      (sum, item) => sum + item.priceAtTime * item.quantity,
      0,
    );

    // Tạo đơn hàng
    const order = await Order.create({
      userId,
      items: cart.items.map((item) => ({
        productId: item.productId,
        productName: item.productName,
        price: item.priceAtTime,
        quantity: item.quantity,
      })),
      totalAmount,
      status: "pending",
      shippingName: shippingName || "",
      shippingPhone: shippingPhone || "",
      shippingAddress: shippingAddress || "",
      paymentMethod: paymentMethod || "cod",
      paymentStatus: "unpaid",
      note: note || "",
    });

    // Xóa giỏ hàng sau khi đặt hàng
    cart.items = [];
    await cart.save();

    res.status(201).json({
      id: order._id,
      userId: order.userId,
      items: order.items,
      totalAmount: order.totalAmount,
      status: order.status,
      shippingName: order.shippingName,
      shippingPhone: order.shippingPhone,
      shippingAddress: order.shippingAddress,
      paymentMethod: order.paymentMethod,
      paymentStatus: order.paymentStatus,
      note: order.note,
      createdAt: order.createdAt,
    });
  } catch (err) {
    console.error("Create order error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

// GET /api/orders/user/:userId
// Buyer xem lịch sử đơn hàng của mình
const getOrdersByUserId = async (req, res) => {
  try {
    const { userId } = req.params;
    const orders = await Order.find({ userId }).sort({ createdAt: -1 });

    const result = orders.map((o) => ({
      id: o._id,
      userId: o.userId,
      items: o.items,
      totalAmount: o.totalAmount,
      status: o.status,
      shippingName: o.shippingName,
      shippingPhone: o.shippingPhone,
      shippingAddress: o.shippingAddress,
      paymentMethod: o.paymentMethod,
      paymentStatus: o.paymentStatus,
      note: o.note,
      createdAt: o.createdAt,
    }));

    res.json(result);
  } catch (err) {
    console.error("Get orders by userId error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

// GET /api/orders
// Admin/Staff xem tất cả đơn hàng
const getAllOrders = async (req, res) => {
  try {
    const orders = await Order.find()
      .sort({ createdAt: -1 })
      .populate("userId", "name email phone");

    const result = orders.map((o) => ({
      id: o._id,
      userId: o.userId?._id,
      buyerName: o.shippingName || o.userId?.name || "",
      buyerEmail: o.userId?.email || "",
      buyerPhone: o.shippingPhone || o.userId?.phone || "",
      items: o.items,
      totalAmount: o.totalAmount,
      status: o.status,
      shippingName: o.shippingName,
      shippingPhone: o.shippingPhone,
      shippingAddress: o.shippingAddress,
      paymentMethod: o.paymentMethod,
      paymentStatus: o.paymentStatus,
      note: o.note,
      orderDate: o.createdAt,
      createdAt: o.createdAt,
    }));

    res.json(result);
  } catch (err) {
    console.error("Get all orders error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

// GET /api/orders/:orderId
// Xem chi tiết một đơn hàng
const getOrderById = async (req, res) => {
  try {
    const order = await Order.findById(req.params.orderId).populate(
      "userId",
      "name email phone",
    );
    if (!order)
      return res.status(404).json({ message: "Không tìm thấy đơn hàng" });

    res.json({
      id: order._id,
      userId: order.userId?._id,
      buyerName: order.shippingName || order.userId?.name || "",
      buyerEmail: order.userId?.email || "",
      buyerPhone: order.shippingPhone || order.userId?.phone || "",
      items: order.items,
      totalAmount: order.totalAmount,
      status: order.status,
      shippingName: order.shippingName,
      shippingPhone: order.shippingPhone,
      shippingAddress: order.shippingAddress,
      paymentMethod: order.paymentMethod,
      paymentStatus: order.paymentStatus,
      note: order.note,
      orderDate: order.createdAt,
      createdAt: order.createdAt,
    });
  } catch (err) {
    console.error("Get order by id error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

// PUT /api/orders/:orderId/status
// Admin/Staff cập nhật trạng thái đơn hàng
const updateOrderStatus = async (req, res) => {
  try {
    const { status } = req.query;
    const validStatuses = [
      "pending",
      "processing",
      "shipping",
      "completed",
      "cancelled",
    ];
    if (!validStatuses.includes(status)) {
      return res.status(400).json({ message: "Trạng thái không hợp lệ" });
    }

    const order = await Order.findByIdAndUpdate(
      req.params.orderId,
      { status },
      { new: true },
    );
    if (!order)
      return res.status(404).json({ message: "Không tìm thấy đơn hàng" });

    res.json({
      id: order._id,
      status: order.status,
      paymentStatus: order.paymentStatus,
    });
  } catch (err) {
    console.error("Update order status error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

// DELETE /api/orders/:orderId
// Admin xóa đơn hàng
const deleteOrder = async (req, res) => {
  try {
    const order = await Order.findByIdAndDelete(req.params.orderId);
    if (!order)
      return res.status(404).json({ message: "Không tìm thấy đơn hàng" });
    res.json({ message: "Đã xóa đơn hàng" });
  } catch (err) {
    console.error("Delete order error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

// PUT /api/orders/:orderId/cancel
// Buyer hủy đơn hàng (chỉ khi pending)
const cancelOrder = async (req, res) => {
  try {
    const order = await Order.findById(req.params.orderId);
    if (!order)
      return res.status(404).json({ message: "Không tìm thấy đơn hàng" });
    if (order.status !== "pending") {
      return res
        .status(400)
        .json({ message: "Chỉ có thể hủy đơn hàng đang chờ xác nhận" });
    }
    order.status = "cancelled";
    await order.save();
    res.json({ id: order._id, status: order.status });
  } catch (err) {
    console.error("Cancel order error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

// POST /api/orders/:orderId/checkout
// Proceed to checkout using Momo payment gateway
const checkoutOrder = async (req, res) => {
  try {
    const orderIdParam = req.params.orderId;
    const { returnUrl, notifyUrl, userId } = req.body; // user payload
    const order = await Order.findById(orderIdParam);
    if (!order) {
      return res.status(404).json({ message: "Không tìm thấy đơn hàng" });
    }
    if (order.status !== "pending" || order.paymentStatus !== "unpaid") {
      return res.status(400).json({ message: "Đơn hàng này không thể thanh toán trực tuyến lúc này" });
    }

    // Momo parameters
    const partnerCode = "MOMO";
    const accessKey = "F8BBA842ECF85";
    const secretkey = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
    const requestId = partnerCode + new Date().getTime();
    const orderIdStr = orderIdParam + "_" + new Date().getTime(); // ensure unique orderId for Momo
    const orderInfo = `Thanh toan don hang ${orderIdParam}`;
    const redirectUrl = returnUrl || "https://momo.vn/return";
    const ipnUrl = notifyUrl || "https://callback.url/notify";
    const amount = order.totalAmount.toString();
    const requestType = "captureWallet";
    const extraData = "";

    // Signature formulation
    const rawSignature = "accessKey=" + accessKey + "&amount=" + amount + "&extraData=" + extraData + "&ipnUrl=" + ipnUrl + "&orderId=" + orderIdStr + "&orderInfo=" + orderInfo + "&partnerCode=" + partnerCode + "&redirectUrl=" + redirectUrl + "&requestId=" + requestId + "&requestType=" + requestType;
    
    const signature = crypto.createHmac("sha256", secretkey)
      .update(rawSignature)
      .digest("hex");

    const requestBody = JSON.stringify({
      partnerCode: partnerCode,
      accessKey: accessKey,
      requestId: requestId,
      amount: amount,
      orderId: orderIdStr,
      orderInfo: orderInfo,
      redirectUrl: redirectUrl,
      ipnUrl: ipnUrl,
      extraData: extraData,
      requestType: requestType,
      signature: signature,
      lang: "vi"
    });

    const options = {
      hostname: "test-payment.momo.vn",
      port: 443,
      path: "/v2/gateway/api/create",
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Content-Length": Buffer.byteLength(requestBody)
      }
    };

    return new Promise((resolve, reject) => {
      const momoReq = https.request(options, (momoRes) => {
        momoRes.setEncoding("utf8");
        let body = "";
        momoRes.on("data", (chunk) => {
          body += chunk;
        });
        momoRes.on("end", () => {
          try {
            const parsedBody = JSON.parse(body);
            if (parsedBody && parsedBody.payUrl) {
              resolve(res.json({ payUrl: parsedBody.payUrl, resultCode: parsedBody.resultCode }));
            } else {
               resolve(res.status(400).json({ message: "Lỗi tạo thanh toán Momo", details: parsedBody }));
            }
          } catch (e) {
            resolve(res.status(500).json({ message: "Lỗi phản hồi từ Momo", error: e.message }));
          }
        });
      });

      momoReq.on("error", (e) => {
        console.error("Momo request error:", e);
        resolve(res.status(500).json({ message: "Lỗi kết nối Momo", error: e.message }));
      });

      momoReq.write(requestBody);
      momoReq.end();
    });

  } catch (err) {
    console.error("Checkout order error:", err);
    res.status(500).json({ message: err.message || "Server error" });
  }
};

// POST /api/orders/momo/callback
// Momo IPN Webhook Callback
const momoCallback = async (req, res) => {
  try {
    const { orderId, resultCode, message } = req.body;
    console.log("Momo IPN callback received:", req.body);

    if (resultCode === 0) {
       // Successful payment
       console.log("Payment successful for order:", orderId);
       
       // extract original order ID (we appended timestamp previously: orderId_timestamp)
       const originalOrderId = orderId.split('_')[0];

       const order = await Order.findById(originalOrderId);
       if(order) {
           order.paymentStatus = "paid";
           order.status = "processing";
           await order.save();
           return res.status(200).json({ message: "Update successful" });
       } else {
           return res.status(404).json({ message: "Order not found" });
       }
    } else {
       console.log(`Payment failed for order ${orderId}: ${message}`);
       return res.status(200).json({ message: "Payment failed acknowledged" }); // Momo still expects 2xx for failed callbacks
    }

  } catch (err) {
    console.error("Momo callback error:", err);
    res.status(500).json({ message: "Internal server error" });
  }
};

module.exports = {
  createOrder,
  getOrdersByUserId,
  getAllOrders,
  getOrderById,
  updateOrderStatus,
  deleteOrder,
  cancelOrder,
  checkoutOrder,
  momoCallback,
};
