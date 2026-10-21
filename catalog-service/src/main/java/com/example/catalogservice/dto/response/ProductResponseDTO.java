package com.example.catalogservice.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {
    private Long id;
    private String sku;
    private String name;
    private BigDecimal basePrice;
    private BigDecimal discountPrice;
    private String description;
    private String primaryImageUrl;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private boolean published;
    private Map<String, String> specifications;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}
