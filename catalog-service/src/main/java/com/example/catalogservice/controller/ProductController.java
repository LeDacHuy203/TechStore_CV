package com.example.catalogservice.controller;

import com.example.catalogservice.dto.request.ProductCreateDTO;
import com.example.catalogservice.dto.request.ProductUpdateDTO;
import com.example.catalogservice.dto.response.ApiResponse;
import com.example.catalogservice.dto.response.ProductResponseDTO;
import com.example.catalogservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/catalog/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 1. Thêm mới sản phẩm có upload ảnh qua MultipartForm
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponseDTO>> createProduct(
            @Valid @ModelAttribute ProductCreateDTO dto) throws IOException {

        ProductResponseDTO created = productService.createProduct(dto);
        return ResponseEntity.ok(ApiResponse.success(created, "Thêm sản phẩm thành công!"));
    }

    // 2. Cập nhật sản phẩm có thể thay đổi ảnh qua MultipartForm
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductUpdateDTO dto) throws IOException {

        ProductResponseDTO updated = productService.updateProduct(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật sản phẩm thành công!"));
    }

    // 3. Lấy chi tiết sản phẩm theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(@PathVariable Long id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product, "Lấy thông tin sản phẩm thành công!"));
    }

    // 4. Lấy chi tiết sản phẩm theo SKU
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductBySku(@PathVariable String sku) {
        ProductResponseDTO product = productService.getProductBySku(sku);
        return ResponseEntity.ok(ApiResponse.success(product, "Lấy thông tin sản phẩm thành công!"));
    }

    // 5. Tìm kiếm & Lọc phân trang (Category, Brand, khoảng giá, keyword, sắp xếp)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponseDTO>>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean published,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        Sort sortOrder = Sort.by(Sort.Direction.fromString(sort.length > 1 ? sort[1] : "desc"), sort[0]);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Page<ProductResponseDTO> result = productService.getProducts(
                categoryId, brandId, minPrice, maxPrice, keyword, published, pageable);

        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách sản phẩm thành công!"));
    }

    // 6. Xóa sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa sản phẩm thành công!"));
    }
}
