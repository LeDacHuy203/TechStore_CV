package com.example.catalogservice.controller;

import com.example.catalogservice.dto.request.BrandRequestDTO;
import com.example.catalogservice.dto.response.ApiResponse;
import com.example.catalogservice.dto.response.BrandResponseDTO;
import com.example.catalogservice.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    // 1. Thêm thương hiệu mới (hỗ trợ upload logo qua form-data)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BrandResponseDTO>> createBrand(
            @Valid @ModelAttribute BrandRequestDTO dto) throws IOException {
        BrandResponseDTO created = brandService.createBrand(dto);
        return ResponseEntity.ok(ApiResponse.success(created, "Thêm thương hiệu thành công!"));
    }

    // 2. Cập nhật thương hiệu (hỗ trợ đổi logo qua form-data)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BrandResponseDTO>> updateBrand(
            @PathVariable Long id,
            @Valid @ModelAttribute BrandRequestDTO dto) throws IOException {
        BrandResponseDTO updated = brandService.updateBrand(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật thương hiệu thành công!"));
    }

    // 3. Lấy chi tiết thương hiệu theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponseDTO>> getBrandById(@PathVariable Long id) {
        BrandResponseDTO brand = brandService.getBrandById(id);
        return ResponseEntity.ok(ApiResponse.success(brand, "Lấy thông tin thương hiệu thành công!"));
    }

    // 4. Lấy toàn bộ danh sách thương hiệu
    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandResponseDTO>>> getAllBrands() {
        List<BrandResponseDTO> list = brandService.getAllBrands();
        return ResponseEntity.ok(ApiResponse.success(list, "Lấy danh sách thương hiệu thành công!"));
    }

    // 5. Xóa thương hiệu
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa thương hiệu thành công!"));
    }
}
