package com.example.paymentservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCreateRequest {

    @NotBlank(message = "Mã đơn hàng không được để trống")
    private String orderCode;

    @NotNull(message = "Số tiền thanh toán không được để trống")
    @DecimalMin(value = "1000.0", message = "Số tiền thanh toán tối thiểu là 1,000 VNĐ")
    private BigDecimal amount;

    private String orderInfo;

    private String ipAddress;
}
