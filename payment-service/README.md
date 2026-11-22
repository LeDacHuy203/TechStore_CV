# Payment Service (:8085) - Dịch vụ Tích hợp Cổng thanh toán VNPay

Payment Service phụ trách xử lý giao dịch thanh toán cho hệ thống TechStore, tích hợp cổng thanh toán trực tuyến **VNPay Sandbox** theo chuẩn VNP 2.1.0 với cơ chế mã hóa bảo mật HMAC-SHA512.

---

## 1. Kiến trúc & Công nghệ
- **Framework:** Spring Boot 3.x / 4.x, Spring Data JPA, Hibernate.
- **Database:** PostgreSQL (`jdbc:postgresql://localhost:5432/paymentdb`).
- **Bảo mật:** Thuật toán mã hóa chữ ký số HMAC-SHA512 đối soát checksum 2 chiều (Request & Callback/IPN).
- **Format chuẩn:** Dữ liệu phản hồi luôn được bao bọc trong `ApiResponse<T>`.

---

## 2. Luồng tích hợp thanh toán VNPay (Payment Flow)

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Khách hàng
    participant Client as Frontend / Web Store
    participant Gateway as API Gateway (:8080)
    participant Payment as Payment Service (:8085)
    participant VNPay as Cổng thanh toán VNPay Sandbox
    participant DB as PostgreSQL (paymentdb)

    Customer->>Client: Bấm "Thanh toán qua VNPay"
    Client->>Gateway: POST /api/v1/payments/create-url
    Gateway->>Payment: Chuyển tiếp Request
    Payment->>DB: Lưu giao dịch với status = PENDING
    Payment->>Payment: Sắp xếp tham số, tính chữ ký HMAC-SHA512
    Payment-->>Client: Trả về paymentUrl
    Client->>Customer: Điều hướng trình duyệt sang paymentUrl (VNPay)
    Customer->>VNPay: Nhập thông tin thẻ/QR và xác thực OTP
    VNPay->>Customer: Chuyển hướng về Return URL kèm query params & chữ ký
    Customer->>Client: Mở URL callback kèm params
    Client->>Gateway: GET /api/v1/payments/vnpay-return?vnp_TxnRef=...
    Gateway->>Payment: Chuyển tiếp kết quả
    Payment->>Payment: Đối soát chữ ký vnp_SecureHash bằng HMAC-SHA512
    alt Chữ ký hợp lệ & vnp_ResponseCode = '00'
        Payment->>DB: Cập nhật status = SUCCESS, lưu transactionNo
        Payment-->>Client: Trả về trạng thái thanh toán THÀNH CÔNG
    else Chữ ký không hợp lệ hoặc thanh toán thất bại
        Payment->>DB: Cập nhật status = FAILED
        Payment-->>Client: Trả về trạng thái thanh toán THẤT BẠI
    end
```

---

## 3. Danh sách RESTful APIs

| Method | Endpoint | Mô tả | Yêu cầu Body / Param |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/payments/create-url` | Tạo link redirect sang VNPay Sandbox | `orderCode`, `amount`, `orderInfo`, `ipAddress` |
| `GET` | `/api/v1/payments/vnpay-return` | Xử lý Callback/Return từ VNPay sau giao dịch | Các tham số query params của VNPay (`vnp_TxnRef`, `vnp_SecureHash`, ...) |
| `GET` | `/api/v1/payments/order/{orderCode}` | Tra cứu lịch sử & trạng thái giao dịch theo mã đơn hàng | Path variable `orderCode` |

---

## 4. Cấu hình VNPay Sandbox (`application.yaml`)

```yaml
vnpay:
  pay-url: "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"
  return-url: "http://localhost:8080/api/v1/payments/vnpay-return"
  tmn-code: "CGXZLS0Z"
  hash-secret: "XNBCJFAKAZQSGTARRLGCHVZWCIOIGSHN"
```
