package com.example.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_spects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//(Thông số cấu hình máy tính)
public class ProductSpect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//    Không cho phép specKey null, độ dài tối đa 50 ký tự
    @Column(nullable = false,length = 50)
    private String specKey; // Key của thông số, ví dụ: "CPU", "RAM", "Storage"
//    Không cho phép specValue null, độ dài tối đa 50 ký tự
    @Column(nullable = false,length = 50)
    private String specValue; // Giá trị của thông số, ví dụ: "Intel Core

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id",nullable = false)
    private Product product; // Sản phẩm mà thông số này thuộc về

}
