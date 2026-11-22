package com.example.orderservice.dto.request;

import com.example.orderservice.entity.OrderStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdateRequest {
    private OrderStatus status;
    private String paymentStatus;
}
