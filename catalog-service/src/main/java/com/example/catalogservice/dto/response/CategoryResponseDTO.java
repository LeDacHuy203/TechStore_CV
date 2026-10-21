package com.example.catalogservice.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDTO {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long parentId;
    private String parentName;

    // Danh sách các danh mục con (dạng cây đa cấp)
    private List<CategoryResponseDTO> subCategories;
}
