package com.example.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String productSku;

    @Builder.Default
    @Column(nullable = false)
    private int quantityInStock = 0;

    @Builder.Default
    @Column(nullable = false)
    private int quantityReserved = 0;

    @Column(length = 50)
    private String warehouseCode;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public int getAvailableQuantity() {
        return Math.max(0, quantityInStock - quantityReserved);
    }
}
