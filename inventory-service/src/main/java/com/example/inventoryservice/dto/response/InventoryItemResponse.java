package com.example.inventoryservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemResponse {

    private Long id;
    private String productSku;
    private int quantityInStock;
    private int quantityReserved;
    private int availableQuantity;
    private String warehouseCode;
    private LocalDateTime updatedAt;
}
