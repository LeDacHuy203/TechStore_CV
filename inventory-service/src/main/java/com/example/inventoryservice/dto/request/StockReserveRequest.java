package com.example.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReserveRequest {

    @NotBlank(message = "Mã SKU không được để trống")
    private String productSku;

    @Min(value = 1, message = "Số lượng giữ kho phải lớn hơn 0")
    private int quantity;
}
