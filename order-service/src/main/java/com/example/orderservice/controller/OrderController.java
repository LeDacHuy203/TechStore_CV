package com.example.orderservice.controller;

import com.example.orderservice.dto.request.CheckoutRequest;
import com.example.orderservice.dto.request.OrderStatusUpdateRequest;
import com.example.orderservice.dto.response.ApiResponse;
import com.example.orderservice.dto.response.OrderResponse;
import com.example.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 1. Tạo đơn hàng (Checkout) với Idempotency-Key chống click đúp
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CheckoutRequest request) {

        OrderResponse order = orderService.processCheckout(idempotencyKey, request);
        return ResponseEntity.ok(ApiResponse.success(order, "Tạo đơn hàng thành công!"));
    }

    // 2. Tra cứu chi tiết đơn hàng theo mã đơn
    @GetMapping("/{orderCode}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByCode(@PathVariable String orderCode) {
        OrderResponse order = orderService.getOrderByCode(orderCode);
        return ResponseEntity.ok(ApiResponse.success(order, "Lấy thông tin đơn hàng thành công!"));
    }

    // 3. Tra cứu lịch sử đơn hàng của người dùng
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUserId(@PathVariable Long userId) {
        List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(orders, "Lấy danh sách đơn hàng thành công!"));
    }

    // 4. Cập nhật trạng thái đơn hàng (Dành cho Quản trị viên hoặc Webhook thanh toán)
    @PutMapping("/{orderCode}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable String orderCode,
            @RequestBody OrderStatusUpdateRequest request) {

        OrderResponse order = orderService.updateOrderStatus(orderCode, request);
        return ResponseEntity.ok(ApiResponse.success(order, "Cập nhật trạng thái đơn hàng thành công!"));
    }
}
