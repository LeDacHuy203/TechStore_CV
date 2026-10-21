package com.example.catalogservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateDTO {
    @NotBlank(message = "Mã SKU không được để trống")
    private String sku;
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;
    @NotNull
    @DecimalMin(value = "0.0",inclusive = false,message = "Giá niêm yết phải lớn hơn 0")
    private BigDecimal basePrice;

    private BigDecimal discountPrice;

    private String description;

    @NotNull(message = "Danh mục sản phẩm không được để trống")
    private Long categoryId;

    private Long brandId;

    // File ảnh gửi lên từ form (multipart/form-data)
    private MultipartFile imageFile;
    // Map các thông số cấu hình: "CPU" -> "Ryzen 7", "RAM" -> "16GB"
    private Map<String, String> specifications;
}
