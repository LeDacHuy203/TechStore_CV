package com.example.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //Quan hệ ManyToOne với User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false,length = 100)
    private String province; // Tinh / Thanh Pho

    @Column(nullable = false,length = 100)
    private String district; // Huyen / Quan

    @Column(nullable = false,length = 100)
    private String ward; // Xa / Phuong

    @Column(nullable = false,length = 255)
    private String detailAddress; // Dia chi chi tiet

    @Builder.Default
    @Column(name = "is_default")
    private boolean isDefault = false; // Địa chỉ mặc định
}
