# Đặc tả API Backend (theo FE - api.js)

FE đã cấu hình gọi API qua proxy (`API_BASE_URL = ""`), tức mọi request `/api/...` sẽ tới backend local (ví dụ `http://localhost:5000`). Backend cần implement đúng các endpoint và body dưới đây.

---

## 1. AUTH (`authAPI`)

- **POST `/api/auth/register`**
  - Body: `{ email, password, fullName, role? }` (role: BUYER | STAFF | MANAGER | ADMIN)
  - Response: object user (có `token` để FE lưu localStorage key `"user"`)

- **POST `/api/auth/login`**
  - Body: `{ email, password }`
  - Response: object user có `token` (FE dùng cho `Authorization: Bearer <token>`)

---

## 2. PRODUCTS (`productAPI`)

- **GET `/api/products`** – danh sách sản phẩm (FE dùng cho homepage)
- **GET `/api/products/:id`** – chi tiết 1 sản phẩm
- **POST `/api/products`** – tạo sản phẩm (admin/staff), body: productData
- **PUT `/api/products`** – cập nhật sản phẩm, body: productData
- **DELETE** (FE gọi qua **PUT** với body `{ ...productData, status: "INACTIVE" }`) – “xóa” = đổi status INACTIVE

Gợi ý format product: `id`, `name`, `category`, `price`, `image` hoặc `imgUrl`, `rating`, `reviews`, `discount`, `originalPrice`, `status`.

---

## 3. CART (`cartAPI`)

- **GET `/api/cart/:userId`**
  - Response: `{ cartItems: [{ id, productId, productName, priceAtTime, quantity }] }`

- **POST `/api/cart/add`**
  - Body: `{ userId, productId, quantity }`
  - Response: cart đã cập nhật (cùng format GET)

- **PUT `/api/cart/item/:cartItemId?quantity=:quantity`** – sửa số lượng
- **DELETE `/api/cart/item/:cartItemId`** – xóa 1 item
- **DELETE `/api/cart/:userId/clear`** – xóa toàn bộ giỏ

---

## 4. ORDERS (`orderAPI`)

- **GET `/api/orders`** – tất cả đơn (admin/staff)
- **GET `/api/orders/:orderId`** – chi tiết 1 đơn
- **GET `/api/orders/user/:userId`** – đơn theo user
- **GET `/api/orders/:orderId/items`** – items của đơn
- **POST `/api/orders`** – tạo đơn, body: `{ userId }` (backend lấy giỏ của user để tạo đơn)
- **PUT `/api/orders/:orderId/status?status=:status`** – cập nhật trạng thái
- **DELETE `/api/orders/:orderId`** – xóa đơn (admin)
- **POST `/api/orders/:orderId/checkout`** – thanh toán MoMo  
  - Body: `{ userId, returnUrl?, notifyUrl? }`  
  - Response: có `payUrl`, `resultCode` (0 = success) để FE redirect sang MoMo

---

## 5. FILES (`fileAPI`)

- **POST `/api/files/upload?folder=:folder`** – upload file (FormData, field `file`)
- **GET `/api/files/url?fileName=:fileName&folder=:folder`** – lấy URL file
- **DELETE `/api/files/delete?fileName=:fileName&folder=:folder`** – xóa file
- **POST `/api/products/:productId/upload-image`** – upload ảnh sản phẩm (FormData, field `file`)

---

## 6. USER (`userAPI`)

- **GET `/api/user`** – danh sách user (admin)
- **GET `/api/user/:id`** – chi tiết 1 user

---

## Ghi chú

- Mọi request (trừ upload) dùng `Content-Type: application/json`.
- FE gửi token: `Authorization: Bearer <token>` (token lấy từ `localStorage.user.token`).
- Lỗi: trả JSON `{ message }` hoặc `{ error }`, status HTTP phù hợp (4xx/5xx).
- CORS: cho phép origin FE (ví dụ `http://localhost:5173`).
