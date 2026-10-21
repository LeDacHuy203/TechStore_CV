package com.example.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Không cho phép name null, độ dài tối đa 200 ký tự
    @Column(nullable = false, length = 200)
    private String name;

    // Không cho phép sku null và phải là duy nhất
    @Column(nullable = false, unique = true, length = 50)
    private String sku; // Mã duy nhất: LAPTOP-ROG-G16-4060

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal basePrice; // Giá niêm yết (Giá gốc)

    private BigDecimal discountPrice; // Giá khuyến mãi (Giá bán thực tế)

    private String primaryImageUrl; // URL hình ảnh chính

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // Danh mục sản phẩm

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand; // Thương hiệu sản phẩm

    // Danh sách thông số kỹ thuật (CPU, RAM, GPU...)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSpect> specs;

    // Bộ sưu tập album nhiều ảnh chi tiết
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;

    @Builder.Default
    private boolean published = true; // Trạng thái hiển thị sản phẩm

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
