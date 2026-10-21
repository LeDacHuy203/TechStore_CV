package com.example.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "brands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//Thương hiệu: ASUS, MSI, Dell, Apple, Gigabyte.
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//    Không cho phép name null, độ dài tối đa 100 ký tự và phải là duy nhất
    @Column(nullable = false,unique = true,length = 100)
    private String name;

    @Column(length = 255)
    private String logoUrl;
}
