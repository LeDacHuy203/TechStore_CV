package com.example.paymentservice.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCallbackResponse {
    private String orderCode;
    private BigDecimal amount;
    private String status; // SUCCESS, FAILED
    private String transactionNo;
    private String bankCode;
    private String responseCode;
    private String message;
}
