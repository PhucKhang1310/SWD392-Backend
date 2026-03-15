var path = require("path");
require("dotenv").config({ path: path.join(__dirname, ".env"), quiet: true });

// Kiểm tra Google OAuth ngay khi khởi động (để dễ debug)
const hasGoogleClientId =
  (process.env.GOOGLE_CLIENT_ID || "").trim().length > 0;
console.log(
  "[Auth] Đăng nhập Google:",
  hasGoogleClientId
    ? "đã cấu hình"
    : "CHƯA cấu hình (thêm GOOGLE_CLIENT_ID vào BE/.env và restart)",
);

var express = require("express");
var cookieParser = require("cookie-parser");
var logger = require("morgan");
var connectDB = require("./config/db");
var { notFound, errorHandler } = require("./middleware/errorMiddleware");

var indexRouter = require("./routes/index");
var usersRouter = require("./routes/users");
var userRoutes = require("./routes/userRoutes");
var authRouter = require("./routes/auth");
var uploadRouter = require("./routes/upload");
var productsRouter = require("./routes/products");
var categoriesRouter = require("./routes/categories");
var cartRouter = require("./routes/cart");
var wishlistRouter = require("./routes/wishlist");
var ordersRouter = require("./routes/orders");
var reviewsRouter = require("./routes/reviews");
var playersRouter = require("./routes/players");
var chatRouter = require("./routes/chat");

connectDB().catch((error) => {
  console.error("[DB] Connection failed:", error.message);
});

var cors = require("cors");
var app = express();

// ---------------------------------------------------------------------------
// CORS Configuration
// ---------------------------------------------------------------------------
// In DEVELOPMENT  → all origins are accepted (fast iteration, no config needed).
// In PRODUCTION   → only origins in the allow-list below are accepted.
//
// Allow-list sources (checked in order):
//  1. FRONTEND_URL env var  – comma-separated list, e.g. "https://myapp.com,https://admin.myapp.com"
//  2. Hard-coded localhost / LAN ports used during local dev
//  3. Expo Go / React-Native (exp://) and local-network IPs (192.168.x, 10.0.x)
//  4. Public tunnel services: ngrok, Cloudflare, localtunnel
//  5. No-origin requests (mobile apps, curl, server-to-server) → always allowed
// ---------------------------------------------------------------------------
const isDev = process.env.NODE_ENV !== "production";

// Build explicit allow-list from env var (supports multiple comma-separated URLs)
const envOrigins = (process.env.FRONTEND_URL || "")
  .split(",")
  .map((s) => s.trim())
  .filter(Boolean);

const allowedOrigins = [
  ...envOrigins,
  // Local web dev
  "http://localhost:3000",
  "http://localhost:5173",
  "http://127.0.0.1:5173",
  // Local mobile dev (Expo / React Native)
  "http://localhost:8081",
  "http://127.0.0.1:8081",
  // Android emulator → host machine
  "http://10.0.2.2:8081",
  "http://10.0.2.2:5000",
];

// Detect public tunnel origins (ngrok, Cloudflare Tunnel, localtunnel)
const isTunnelOrigin = (origin) =>
  typeof origin === "string" &&
  (/\.ngrok\.(io|dev|app)$/i.test(origin) ||
    /\.trycloudflare\.com$/i.test(origin) ||
    /\.loca\.lt$/i.test(origin) ||
    /\.ngrok-free\.app$/i.test(origin));

// Detect local-network / Expo origins
const isLocalNetworkOrigin = (origin) =>
  typeof origin === "string" &&
  (origin.startsWith("exp://") ||
    origin.startsWith("http://192.168.") ||
    origin.startsWith("http://10.0.") ||
    origin.startsWith("http://172."));

const corsOptions = {
  origin: (origin, cb) => {
    // No origin header → mobile app, curl, or server-to-server → allow
    if (!origin) return cb(null, true);
    // Development: allow everything
    if (isDev) return cb(null, true);
    // Production checks
    if (allowedOrigins.includes(origin)) return cb(null, true);
    if (isLocalNetworkOrigin(origin)) return cb(null, true);
    if (isTunnelOrigin(origin)) return cb(null, true);
    // Blocked
    console.warn(`[CORS] Blocked origin: ${origin}`);
    cb(null, false);
  },
  credentials: true, // Allow cookies / Authorization headers
  methods: ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"],
  allowedHeaders: [
    "Content-Type",
    "Authorization",
    "X-Requested-With",
    "Accept",
    "Origin",
  ],
  exposedHeaders: ["Content-Disposition"], // Useful for file downloads
  optionsSuccessStatus: 204, // Some legacy browsers choke on 204; change to 200 if needed
};

app.use(cors(corsOptions));
// Explicitly handle preflight OPTIONS requests for all routes
app.options("*", cors(corsOptions));
app.use(logger("dev"));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cookieParser());

app.use(express.static(path.join(__dirname, "public")));
app.use("/uploads", express.static(path.join(__dirname, "uploads")));

app.use("/users", usersRouter);
app.use("/api/users", userRoutes);
app.use("/api/auth", authRouter);
app.use("/api/files", uploadRouter);
app.use("/api/products", productsRouter);
app.use("/api/categories", categoriesRouter);
app.use("/api/cart", cartRouter);
app.use("/api/wishlist", wishlistRouter);
app.use("/api/orders", ordersRouter);
app.use("/api/reviews", reviewsRouter);
app.use("/api/players", playersRouter);
app.use("/api/chat", chatRouter);

// Gộp tunnel: khi SERVE_FE=1, BE vừa serve API vừa serve FE build → chỉ cần 1 ngrok đến port 5000
const serveFe = process.env.SERVE_FE === "1" || process.env.SERVE_FE === "true";
if (serveFe) {
  const feDist = path.join(__dirname, "..", "FE", "dist");
  app.use(express.static(feDist));
  app.get("*", (req, res) => res.sendFile(path.join(feDist, "index.html")));
} else {
  app.use("/", indexRouter);
}

app.use(notFound);
app.use(errorHandler);

module.exports = app;
