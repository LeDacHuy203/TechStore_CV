package com.example.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "serial_numbers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SerialNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String serialCode; // Ví dụ: SN-ASUS-2026-998822

    @Column(nullable = false, length = 50)
    private String productSku;

    private Long orderId; // Logical Reference tới đơn hàng

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SerialStatus status = SerialStatus.IN_STOCK;

    private LocalDateTime warrantyStartDate;
    private LocalDateTime warrantyEndDate;
}
