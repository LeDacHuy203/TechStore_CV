package com.example.notificationservice.controller;

import com.example.notificationservice.dto.request.EmailNotificationRequest;
import com.example.notificationservice.dto.request.OrderNotificationRequest;
import com.example.notificationservice.dto.response.ApiResponse;
import com.example.notificationservice.dto.response.NotificationResponse;
import com.example.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send-email")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendEmail(
            @Valid @RequestBody EmailNotificationRequest request) {
        NotificationResponse response = notificationService.sendEmail(request);
        return ResponseEntity.ok(ApiResponse.<NotificationResponse>builder()
                .code(200)
                .message("Đã xử lý gửi thông báo email")
                .data(response)
                .build());
    }

    @PostMapping("/order-success")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendOrderSuccessNotification(
            @Valid @RequestBody OrderNotificationRequest request) {
        NotificationResponse response = notificationService.sendOrderSuccessNotification(request);
        return ResponseEntity.ok(ApiResponse.<NotificationResponse>builder()
                .code(200)
                .message("Gửi thông báo xác nhận đơn hàng thành công")
                .data(response)
                .build());
    }

    @GetMapping("/recipient/{recipient}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getByRecipient(
            @PathVariable String recipient) {
        List<NotificationResponse> list = notificationService.getNotificationsByRecipient(recipient);
        return ResponseEntity.ok(ApiResponse.<List<NotificationResponse>>builder()
                .code(200)
                .message("Lấy lịch sử thông báo thành công")
                .data(list)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getById(
            @PathVariable Long id) {
        NotificationResponse response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(ApiResponse.<NotificationResponse>builder()
                .code(200)
                .message("Lấy thông tin chi tiết thông báo thành công")
                .data(response)
                .build());
    }
}
