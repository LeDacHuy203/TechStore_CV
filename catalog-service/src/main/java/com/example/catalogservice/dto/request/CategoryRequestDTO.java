package com.example.catalogservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequestDTO {

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    private String slug;

    private String description;

    // ID danh mục cha (để tạo danh mục đa cấp, vd: Laptop -> Laptop Gaming -> ASUS ROG)
    private Long parentId;
}
