package com.example.orderservice.dto.response;

import com.example.orderservice.entity.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private String orderCode;
    private String idempotencyKey;
    private Long userId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddress;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private OrderStatus status;
    private String paymentMethod;
    private String paymentStatus;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
}
