# 📦 INVENTORY & WARRANTY SERVICE

> **Dịch vụ quản lý Tồn kho, Khóa giữ tồn kho tạm thời, Quản lý số Serial/IMEI & Kích hoạt bảo hành**

---

## 📌 1. TỔNG QUAN DỊCH VỤ

* **Cổng chạy (Port):** `8083`
* **Cơ sở dữ liệu:** PostgreSQL (`inventorydb`)
* **Vai trò chính:**
  1. **Quản lý Tồn kho:** Số lượng tồn thực tế (`quantityInStock`), số lượng đang bị khóa giữ (`quantityReserved`), số lượng khả dụng (`availableQuantity = inStock - reserved`).
  2. **Cơ chế Reserve & Release:** Khóa tồn kho tạm khi khách đặt hàng, tự động hoàn trả khi hủy đơn nhằm **ngăn ngừa triệt để Race Condition**.
  3. **Quản lý Serial & Bảo hành:** Theo dõi số Serial/IMEI của từng chiếc máy tính/linh kiện (`SerialNumber`), kích hoạt thời hạn bảo hành (24 - 36 tháng) khi hoàn tất đơn hàng.

---

## 📡 2. DANH SÁCH API ENDPOINTS

| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| `GET` | `/api/v1/inventory/{sku}` | Kiểm tra thông tin và số lượng khả dụng theo SKU |
| `POST` | `/api/v1/inventory/update` | Nhập kho / Cập nhật số lượng tồn kho |
| `POST` | `/api/v1/inventory/reserve` | **Khóa giữ kho tạm thời** (dành cho Order Service) |
| `POST` | `/api/v1/inventory/release` | **Hoàn trả kho** khi đơn hàng bị hủy hoặc quá hạn |
| `POST` | `/api/v1/inventory/deduct` | **Trừ kho thực tế** khi đơn hàng thanh toán thành công |
| `POST` | `/api/v1/inventory/warranty/activate` | **Kích hoạt bảo hành** theo số Serial và mã đơn hàng |
| `GET` | `/api/v1/inventory/warranty/order/{orderId}` | Lấy danh sách số Serial đã mua theo đơn hàng |
