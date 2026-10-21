package com.example.catalogservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandRequestDTO {

    @NotBlank(message = "Tên thương hiệu không được để trống")
    private String name;

    private String logoUrl;

    // File logo tải lên Cloudinary nếu có
    private MultipartFile logoFile;
}
