package com.example.orderservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutItemRequest {

    @NotBlank(message = "Mã SKU không được để trống")
    private String productSku;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String productName;

    @Min(value = 1, message = "Số lượng mua tối thiểu là 1")
    private int quantity;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0")
    private BigDecimal price;
}
