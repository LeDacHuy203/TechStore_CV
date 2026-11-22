package com.example.notificationservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderNotificationRequest {

    @NotBlank(message = "Email người nhận không được để trống")
    @Email(message = "Email người nhận không đúng định dạng")
    private String recipient;

    private String customerName;

    @NotBlank(message = "Mã đơn hàng không được để trống")
    private String orderCode;

    @NotNull(message = "Tổng tiền không được để trống")
    private BigDecimal totalAmount;

    private String paymentMethod;

    private String itemsSummary;
}
