# 🚀 SỔ TAY HƯỚNG DẪN TỰ CODE TAY DỰ ÁN TECHSTORE MICROSERVICES

> **Dành cho Lập trình viên:** Hướng dẫn chi tiết từng dòng lệnh, từng class để bạn tự tay gõ (code tay) toàn bộ hệ thống Microservices E-Commerce Máy tính & Laptop.  
> **Giao diện Web:** Bạn có thể mở trực tiếp file [`huong_dan_code.html`](./huong_dan_code.html) và [`tien_trinh_du_an.html`](./tien_trinh_du_an.html) trên trình duyệt để vừa đọc code mẫu vừa tick chọn tiến độ.

---

## 📑 MỤC LỤC HƯỚNG DẪN CODE TAY
- [0. Chuẩn Bị Môi Trường & Cơ Sở Dữ Liệu PostgreSQL](#0-chuẩn-bị-môi-trường--cơ-sở-dữ-liệu-postgresql)
- [1. Khởi Tạo Gradle Multi-Project (Root Project)](#1-khởi-tạo-gradle-multi-project-root-project)
- [2. Chuẩn Hóa Lớp Dùng Chung (ApiResponse & Global Exception)](#2-chuẩn-hóa-lớp-dùng-chung-apiresponse--global-exception)
- [3. Code Tay Auth & User Service (:8081)](#3-code-tay-auth--user-service-8081)
- [4. Code Tay Product Catalog Service (:8082 - Laptop, PC & Cloudinary)](#4-code-tay-product-catalog-service-8082---laptop-pc--cloudinary)
- [5. Code Tay Inventory & Warranty Service (:8083 - Serial & Bảo Hành)](#5-code-tay-inventory--warranty-service-8083---serial--bảo-hành)
- [6. Code Tay Order & Cart Service (:8084 - Giỏ Hàng & Checkout)](#6-code-tay-order--cart-service-8084---giỏ-hàng--checkout)
- [7. Code Tay Payment Service (:8085 - VNPay & Webhook IPN)](#7-code-tay-payment-service-8085---vnpay--webhook-ipn)
- [8. Code Tay API Gateway (:8080 - Routing & JWT Filter)](#8-code-tay-api-gateway-8080---routing--jwt-filter)

---

## 0. CHUẨN BỊ MÔI TRƯỜNG & CƠ SỞ DỮ LIỆU POSTGRESQL

Trước khi bắt đầu gõ code, bạn cần tạo 6 database độc lập trên PostgreSQL (mô hình Database-per-Service):

Mở công cụ **pgAdmin 4** hoặc **DBeaver** hoặc chạy bằng terminal SQL:
```sql
CREATE DATABASE auth_db;
CREATE DATABASE catalog_db;
CREATE DATABASE inventory_db;
CREATE DATABASE order_db;
CREATE DATABASE payment_db;
CREATE DATABASE notification_db;
```

---

## 1. KHỞI TẠO GRADLE MULTI-PROJECT (ROOT PROJECT)

### Bước 1.1: Tạo file `settings.gradle` tại thư mục gốc
File này dùng để định nghĩa các submodules trong dự án:

```groovy
rootProject.name = 'techstore-microservices'

include 'api-gateway'
include 'auth-service'
include 'catalog-service'
include 'inventory-service'
include 'order-service'
include 'payment-service'
include 'notification-service'
```

### Bước 1.2: Tạo file `build.gradle` tại thư mục gốc
Khai báo plugin Spring Boot, JDK 17 và cấu hình chung cho tất cả các submodules:

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.4' apply false
    id 'io.spring.dependency-management' version '1.1.6' apply false
}

allprojects {
    group = 'com.techstore'
    version = '1.0.0-SNAPSHOT'
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        // Lombok dùng chung cho tất cả các service
        compileOnly 'org.projectlombok:lombok'
        annotationProcessor 'org.projectlombok:lombok'
        testCompileOnly 'org.projectlombok:lombok'
        testAnnotationProcessor 'org.projectlombok:lombok'
    }
}
```

---

## 2. CHUẨN HÓA LỚP DÙNG CHUNG (APIRESPONSE & GLOBAL EXCEPTION)

Mỗi service đều cần tuân thủ cấu trúc phản hồi chuẩn RESTful. Bạn sẽ gõ 2 class này vào thư mục của từng service (hoặc module common):

### Bước 2.1: Class bọc phản hồi `ApiResponse.java`
*Đường dẫn mẫu:* `<service>/src/main/java/com/techstore/<service>/dto/response/ApiResponse.java*

```java
package com.techstore.catalog.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Thao tác thành công");
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}
```

### Bước 2.2: Class xử lý lỗi toàn cục `GlobalExceptionHandler.java`
*Đường dẫn mẫu:* `<service>/src/main/java/com/techstore/<service>/exception/GlobalExceptionHandler.java*

```java
package com.techstore.catalog.exception;

import com.techstore.catalog.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Xử lý lỗi validate form dữ liệu (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("code", 400);
        body.put("message", "Dữ liệu không hợp lệ");
        body.put("errors", errors);

        return ResponseEntity.badRequest().body(body);
    }

    // 2. Xử lý lỗi không tìm thấy tài nguyên
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, ex.getMessage()));
    }

    // 3. Xử lý ngoại lệ chung
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Lỗi hệ thống: " + ex.getMessage()));
    }
}
```

---

## 3. CODE TAY AUTH & USER SERVICE (:8081)

Dịch vụ quản lý tài khoản khách hàng, nhân viên, phân quyền và cấp Access Token/Refresh Token.

### Bước 3.1: File cấu hình `auth-service/build.gradle`
```groovy
plugins {
    id 'org.springframework.boot'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    runtimeOnly 'org.postgresql:postgresql'

    // JJWT (Java JWT)
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
}
```

### Bước 3.2: Entity `User.java`
*Đường dẫn:* `auth-service/src/main/java/com/techstore/auth/entity/User.java`

```java
package com.techstore.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fullName;
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role; // ROLE_CUSTOMER, ROLE_STAFF, ROLE_ADMIN

    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Bước 3.3: DTO Đăng ký & Đăng nhập
*Đường dẫn:* `auth-service/src/main/java/com/techstore/auth/dto/request/RegisterRequest.java`

```java
package com.techstore.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = "Username không được để trống")
    @Size(min = 4, max = 50, message = "Username phải từ 4-50 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    private String password;

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    private String phoneNumber;
}
```

### Bước 3.4: Controller `AuthController.java`
*Đường dẫn:* `auth-service/src/main/java/com/techstore/auth/controller/AuthController.java`

```java
package com.techstore.auth.controller;

import com.techstore.auth.dto.request.LoginRequest;
import com.techstore.auth.dto.request.RegisterRequest;
import com.techstore.auth.dto.response.ApiResponse;
import com.techstore.auth.dto.response.TokenResponse;
import com.techstore.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký tài khoản thành công!"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest req) {
        TokenResponse tokens = authService.login(req);
        return ResponseEntity.ok(ApiResponse.success(tokens, "Đăng nhập thành công!"));
    }
}
```

---

## 4. CODE TAY PRODUCT CATALOG SERVICE (:8082 - LAPTOP, PC & CLOUDINARY)

Dịch vụ quản lý toàn bộ sản phẩm (Laptop, PC, Linh kiện máy tính), cấu hình thông số kỹ thuật (CPU, RAM, GPU, Ổ cứng, Màn hình) và tích hợp tải ảnh lên Cloudinary.

### Bước 4.1: File cấu hình `catalog-service/build.gradle`
```groovy
plugins {
    id 'org.springframework.boot'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    runtimeOnly 'org.postgresql:postgresql'

    // Cloudinary SDK
    implementation 'com.cloudinary:cloudinary-http44:1.39.0'
}
```

### Bước 4.2: Entity `Product.java`
*Đường dẫn:* `catalog-service/src/main/java/com/techstore/catalog/entity/Product.java`

```java
package com.techstore.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku; // Ví dụ: LAPTOP-ROG-G16-4060

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private BigDecimal basePrice;

    private BigDecimal discountPrice;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String primaryImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSpec> specs; // CPU, RAM, GPU, Màn hình...

    @Builder.Default
    private boolean published = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### Bước 4.3: DTO Nhận Dữ Liệu Tạo Laptop/PC (`ProductCreateDTO.java`)
> **Lưu ý quan trọng:** Để upload ảnh cùng form dữ liệu, ta dùng `MultipartFile` và Controller dùng `@ModelAttribute` thay vì `@RequestBody`.

*Đường dẫn:* `catalog-service/src/main/java/com/techstore/catalog/dto/request/ProductCreateDTO.java`

```java
package com.techstore.catalog.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ProductCreateDTO {
    @NotBlank(message = "Mã SKU không được để trống")
    private String sku;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá bán phải lớn hơn 0")
    private BigDecimal basePrice;

    private BigDecimal discountPrice;
    private String description;

    @NotNull(message = "Danh mục không được để trống")
    private UUID categoryId;

    // Ảnh sản phẩm gửi từ form
    private MultipartFile imageFile;

    // Thông số kỹ thuật dạng Key-Value (CPU: i9-14900HX, RAM: 32GB, GPU: RTX 4060)
    private Map<String, String> specifications;
}
```

### Bước 4.4: Service Xử Lý Upload Ảnh Lên Cloudinary
*Đường dẫn:* `catalog-service/src/main/java/com/techstore/catalog/service/impl/ProductServiceImpl.java`

```java
package com.techstore.catalog.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.techstore.catalog.dto.request.ProductCreateDTO;
import com.techstore.catalog.entity.Category;
import com.techstore.catalog.entity.Product;
import com.techstore.catalog.entity.ProductSpec;
import com.techstore.catalog.exception.ResourceNotFoundException;
import com.techstore.catalog.repository.ICategoryRepository;
import com.techstore.catalog.repository.IProductRepository;
import com.techstore.catalog.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final IProductRepository productRepository;
    private final ICategoryRepository categoryRepository;
    private final Cloudinary cloudinary;

    @Override
    @Transactional
    public Product createProduct(ProductCreateDTO dto) throws IOException {
        // 1. Kiểm tra danh mục
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục!"));

        // 2. Upload ảnh lên Cloudinary nếu có file
        String imageUrl = null;
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(
                    dto.getImageFile().getBytes(),
                    ObjectUtils.asMap("folder", "techstore/products")
            );
            imageUrl = uploadResult.get("secure_url").toString();
        }

        // 3. Khởi tạo đối tượng Product
        Product product = Product.builder()
                .sku(dto.getSku())
                .name(dto.getName())
                .basePrice(dto.getBasePrice())
                .discountPrice(dto.getDiscountPrice())
                .description(dto.getDescription())
                .primaryImageUrl(imageUrl)
                .category(category)
                .build();

        // 4. Lưu thông số kỹ thuật (Specs)
        if (dto.getSpecifications() != null) {
            List<ProductSpec> specList = new ArrayList<>();
            dto.getSpecifications().forEach((k, v) -> {
                specList.add(ProductSpec.builder()
                        .product(product)
                        .specKey(k)
                        .specValue(v)
                        .build());
            });
            product.setSpecs(specList);
        }

        return productRepository.save(product);
    }
}
```

### Bước 4.5: Controller Nhận `@ModelAttribute`
*Đường dẫn:* `catalog-service/src/main/java/com/techstore/catalog/controller/ProductController.java`

```java
package com.techstore.catalog.controller;

import com.techstore.catalog.dto.request.ProductCreateDTO;
import com.techstore.catalog.dto.response.ApiResponse;
import com.techstore.catalog.entity.Product;
import com.techstore.catalog.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/catalog/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    // Sử dụng consumes multipart/form-data và @ModelAttribute để nhận cả file và dữ liệu
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Product>> createProduct(@Valid @ModelAttribute ProductCreateDTO dto) throws IOException {
        Product created = productService.createProduct(dto);
        return ResponseEntity.ok(ApiResponse.success(created, "Thêm sản phẩm thành công!"));
    }
}
```

---

## 5. CODE TAY INVENTORY & WARRANTY SERVICE (:8083 - SERIAL & BẢO HÀNH)

Đặc thù ngành kinh doanh máy tính là mỗi sản phẩm bán ra đều có **Mã Serial Number / IMEI** để quản lý nguồn gốc linh kiện và tra cứu hạn bảo hành.

### Bước 5.1: Entity `SerialNumber.java`
*Đường dẫn:* `inventory-service/src/main/java/com/techstore/inventory/entity/SerialNumber.java`

```java
package com.techstore.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "serial_numbers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SerialNumber {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String serialCode; // Ví dụ: SN-ASUS-2026-998822

    @Column(nullable = false, length = 50)
    private String productSku;

    private UUID orderId; // Logical Reference tới đơn hàng đã mua

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SerialStatus status; // IN_STOCK, RESERVED, SOLD, UNDER_WARRANTY

    private LocalDateTime warrantyStartDate;
    private LocalDateTime warrantyEndDate; // Ví dụ: 24 tháng sau ngày mua
}
```

---

## 6. CODE TAY ORDER & CART SERVICE (:8084 - GIỎ HÀNG & CHECKOUT)

### Bước 6.1: Quy Trình Checkout với `Idempotency-Key`
Chống tình trạng khách bấm nút Đặt hàng 2 lần liên tiếp gây tạo đơn trùng lặp.

```java
@PostMapping("/checkout")
public ResponseEntity<ApiResponse<OrderResponse>> checkout(
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @Valid @RequestBody CheckoutRequest request) {
    OrderResponse order = orderService.processCheckout(idempotencyKey, request);
    return ResponseEntity.ok(ApiResponse.success(order, "Tạo đơn hàng thành công!"));
}
```

---

## 7. CODE TAY PAYMENT SERVICE (:8085 - VNPAY & WEBHOOK IPN)

### Bước 7.1: Thuật Toán Sinh URL Thanh Toán VNPay kèm Checksum HMAC-SHA512
*Đường dẫn:* `payment-service/src/main/java/com/techstore/payment/service/impl/VNPayServiceImpl.java`

```java
public String createPaymentUrl(OrderPaymentInfo info) {
    Map<String, String> vnp_Params = new HashMap<>();
    vnp_Params.put("vnp_Version", "2.1.0");
    vnp_Params.put("vnp_Command", "pay");
    vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
    vnp_Params.put("vnp_Amount", String.valueOf(info.getAmount().multiply(new BigDecimal(100)).longValue()));
    vnp_Params.put("vnp_CurrCode", "VND");
    vnp_Params.put("vnp_TxnRef", info.getOrderCode());
    vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + info.getOrderCode());
    vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
    vnp_Params.put("vnp_IpAddr", "127.0.0.1");

    // Sắp xếp Alphabet và băm SHA512 với secret key
    String hashData = VNPayUtil.buildHashData(vnp_Params);
    String secureHash = VNPayUtil.hmacSHA512(vnp_HashSecret, hashData);
    
    return vnp_PayUrl + "?" + hashData + "&vnp_SecureHash=" + secureHash;
}
```

---

## 8. CODE TAY API GATEWAY (:8080 - ROUTING & JWT FILTER)

### Bước 8.1: File `application.properties` Gateway Định Tuyến
*Đường dẫn:* `api-gateway/src/main/resources/application.properties`

```properties
server.port=8080
spring.application.name=api-gateway

# ROUTING TỚI CÁC DỊCH VỤ CON
spring.cloud.gateway.routes[0].id=auth-service
spring.cloud.gateway.routes[0].uri=http://localhost:8081
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/v1/auth/**, /api/v1/users/**

spring.cloud.gateway.routes[1].id=catalog-service
spring.cloud.gateway.routes[1].uri=http://localhost:8082
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/v1/catalog/**

spring.cloud.gateway.routes[2].id=inventory-service
spring.cloud.gateway.routes[2].uri=http://localhost:8083
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/v1/inventory/**

spring.cloud.gateway.routes[3].id=order-service
spring.cloud.gateway.routes[3].uri=http://localhost:8084
spring.cloud.gateway.routes[3].predicates[0]=Path=/api/v1/orders/**

spring.cloud.gateway.routes[4].id=payment-service
spring.cloud.gateway.routes[4].uri=http://localhost:8085
spring.cloud.gateway.routes[4].predicates[0]=Path=/api/v1/payments/**
```

---

## 🛠️ LỆNH CHẠY & KIỂM THỬ TỪNG SERVICE

Khi code xong từng service, bạn mở Terminal và chạy lệnh sau để kiểm tra biên dịch:

```powershell
# Biên dịch toàn bộ các dịch vụ
.\gradlew build -x test

# Khởi chạy riêng dịch vụ Catalog
.\gradlew :catalog-service:bootRun

# Khởi chạy riêng Auth Service
.\gradlew :auth-service:bootRun
```

👉 Hãy mở file [`huong_dan_code.html`](./huong_dan_code.html) để xem hướng dẫn trên giao diện Web với tính năng Copy Code và đánh dấu tiến trình tiện lợi!
