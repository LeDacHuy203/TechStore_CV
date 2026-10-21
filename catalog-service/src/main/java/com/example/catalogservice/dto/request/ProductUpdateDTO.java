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
public class ProductUpdateDTO {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @NotNull(message = "Giá niêm yết không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá niêm yết phải lớn hơn 0")
    private BigDecimal basePrice;

    private BigDecimal discountPrice;

    private String description;

    @NotNull(message = "Danh mục sản phẩm không được để trống")
    private Long categoryId;

    private Long brandId;

    private Boolean published; // Cho phép bật/tắt hiển thị sản phẩm

    // File ảnh mới (tùy chọn: nếu gửi lên thì thay thế ảnh cũ trên Cloudinary)
    private MultipartFile imageFile;

    // Cập nhật lại thông số kỹ thuật (CPU, RAM, GPU...)
    private Map<String, String> specifications;
}