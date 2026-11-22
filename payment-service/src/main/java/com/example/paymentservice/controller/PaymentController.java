package com.example.paymentservice.controller;

import com.example.paymentservice.dto.request.PaymentCreateRequest;
import com.example.paymentservice.dto.response.ApiResponse;
import com.example.paymentservice.dto.response.PaymentCallbackResponse;
import com.example.paymentservice.dto.response.PaymentCreateResponse;
import com.example.paymentservice.entity.PaymentTransaction;
import com.example.paymentservice.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;

    @PostMapping("/create-url")
    public ResponseEntity<ApiResponse<PaymentCreateResponse>> createPaymentUrl(
            @Valid @RequestBody PaymentCreateRequest request,
            HttpServletRequest servletRequest) {
        if (request.getIpAddress() == null || request.getIpAddress().isBlank()) {
            request.setIpAddress(servletRequest.getRemoteAddr());
        }
        PaymentCreateResponse response = vnPayService.createPaymentUrl(request);
        return ResponseEntity.ok(ApiResponse.<PaymentCreateResponse>builder()
                .code(200)
                .message("Tạo URL thanh toán VNPay thành công")
                .data(response)
                .build());
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<ApiResponse<PaymentCallbackResponse>> vnpayReturn(
            @RequestParam Map<String, String> queryParams) {
        PaymentCallbackResponse response = vnPayService.processReturn(queryParams);
        return ResponseEntity.ok(ApiResponse.<PaymentCallbackResponse>builder()
                .code(200)
                .message("Xử lý kết quả thanh toán VNPay hoàn tất")
                .data(response)
                .build());
    }

    @GetMapping("/order/{orderCode}")
    public ResponseEntity<ApiResponse<PaymentTransaction>> getTransaction(
            @PathVariable String orderCode) {
        PaymentTransaction transaction = vnPayService.getTransactionByOrderCode(orderCode);
        return ResponseEntity.ok(ApiResponse.<PaymentTransaction>builder()
                .code(200)
                .message("Lấy thông tin giao dịch thành công")
                .data(transaction)
                .build());
    }
}
