package com.example.notificationservice.service;

import com.example.notificationservice.dto.request.EmailNotificationRequest;
import com.example.notificationservice.dto.request.OrderNotificationRequest;
import com.example.notificationservice.dto.response.NotificationResponse;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.exception.ResourceNotFoundException;
import com.example.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Transactional
    public NotificationResponse sendEmail(EmailNotificationRequest request) {
        String status = "SENT";
        try {
            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(request.getRecipient());
                message.setSubject(request.getSubject());
                message.setText(request.getContent());
                mailSender.send(message);
                log.info("Đã gửi email thành công tới {}", request.getRecipient());
            } else {
                log.warn("JavaMailSender chưa được cấu hình. Gửi email dạng Mock/Log cho {}", request.getRecipient());
            }
        } catch (Exception e) {
            log.error("Lỗi khi gửi email tới {}: {}", request.getRecipient(), e.getMessage());
            status = "FAILED";
        }

        Notification notification = Notification.builder()
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .content(request.getContent())
                .type(request.getType() != null ? request.getType() : "GENERAL")
                .referenceId(request.getReferenceId())
                .status(status)
                .build();

        Notification saved = notificationRepository.save(notification);
        return mapToResponse(saved);
    }

    @Transactional
    public NotificationResponse sendOrderSuccessNotification(OrderNotificationRequest req) {
        String customer = req.getCustomerName() != null ? req.getCustomerName() : "Quý khách";
        String subject = "TechStore - Xác nhận đơn hàng thành công #" + req.getOrderCode();
        String content = String.format(
                "Xin chào %s,\n\n" +
                "Đơn hàng mã %s của quý khách đã được tạo thành công trên hệ thống TechStore!\n" +
                "- Tổng giá trị thanh toán: %,.0f VNĐ\n" +
                "- Phương thức thanh toán: %s\n" +
                "- Chi tiết sản phẩm: %s\n\n" +
                "TechStore sẽ sớm liên hệ đóng gói và giao sản phẩm đến địa chỉ của quý khách.\n" +
                "Trân trọng cảm ơn quý khách!",
                customer,
                req.getOrderCode(),
                req.getTotalAmount().doubleValue(),
                req.getPaymentMethod() != null ? req.getPaymentMethod() : "VNPAY",
                req.getItemsSummary() != null ? req.getItemsSummary() : "Xem chi tiết trong lịch sử đơn hàng"
        );

        EmailNotificationRequest emailReq = EmailNotificationRequest.builder()
                .recipient(req.getRecipient())
                .subject(subject)
                .content(content)
                .type("ORDER_CONFIRMED")
                .referenceId(req.getOrderCode())
                .build();

        return sendEmail(emailReq);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByRecipient(String recipient) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo với ID: " + id));
        return mapToResponse(notification);
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .recipient(n.getRecipient())
                .subject(n.getSubject())
                .content(n.getContent())
                .type(n.getType())
                .referenceId(n.getReferenceId())
                .status(n.getStatus())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
