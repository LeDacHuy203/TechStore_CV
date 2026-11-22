package com.example.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockUpdateRequest {

    @NotBlank(message = "Mã SKU không được để trống")
    private String productSku;

    @Min(value = 0, message = "Số lượng nhập kho không được âm")
    private int quantityInStock;

    private String warehouseCode;
}
