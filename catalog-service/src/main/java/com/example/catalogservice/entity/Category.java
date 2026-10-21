package com.example.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Không cho phép name null, độ dài tối đa 100 ký tự và phải là duy nhất
    @Column(nullable = false, length = 100, unique = true)
    private String name;

    // Không cho phép slug null, độ dài tối đa 100 ký tự và phải là duy nhất
    @Column(nullable = false, length = 100, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Một danh mục có thể thuộc về 1 danh mục cha (hỗ trợ cây danh mục đa cấp)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // Danh sách các danh mục con
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> subCategories;
}
