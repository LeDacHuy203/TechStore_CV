package com.example.paymentservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCreateResponse {
    private String orderCode;
    private String paymentUrl;
}
