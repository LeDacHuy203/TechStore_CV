# 🛒 ORDER & CART SERVICE

> **Dịch vụ quản lý Giỏ hàng, Đặt hàng & Xử lý Checkout với cơ chế chống click đúp Idempotency-Key**

---

## 📌 1. TỔNG QUAN DỊCH VỤ

* **Cổng chạy (Port):** `8084`
* **Cơ sở dữ liệu:** PostgreSQL (`orderdb`)
* **Vai trò chính:**
  1. **Quy trình Checkout An Toàn:** Tiếp nhận đơn hàng, tính toán tổng tiền, mã giảm giá.
  2. **Idempotency-Key:** Chặn đứng tình trạng khách hàng bấm nút Đặt hàng liên tiếp 2 lần do mạng lag hoặc cố tình gửi 2 request, đảm bảo **chỉ có 1 đơn duy nhất được tạo ra**.
  3. **Quản lý trạng thái đơn:** `PENDING` $\rightarrow$ `CONFIRMED` $\rightarrow$ `SHIPPING` $\rightarrow$ `COMPLETED` / `CANCELLED`.

---

## 📡 2. DANH SÁCH API ENDPOINTS

| Method | Endpoint | Headers | Mô tả |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/orders/checkout` | `Idempotency-Key: <UUID>` | **Tạo đơn hàng mới an toàn (Checkout)** |
| `GET` | `/api/v1/orders/{orderCode}` | - | Tra cứu chi tiết đơn hàng theo mã đơn |
| `GET` | `/api/v1/orders/user/{userId}` | - | Lịch sử mua hàng của khách |
| `PUT` | `/api/v1/orders/{orderCode}/status` | - | Cập nhật trạng thái đơn và thanh toán |
