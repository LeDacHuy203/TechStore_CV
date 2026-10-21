package com.example.catalogservice.controller;

import com.example.catalogservice.dto.request.CategoryRequestDTO;
import com.example.catalogservice.dto.response.ApiResponse;
import com.example.catalogservice.dto.response.CategoryResponseDTO;
import com.example.catalogservice.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 1. Thêm mới danh mục
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> createCategory(
            @Valid @RequestBody CategoryRequestDTO dto) {
        CategoryResponseDTO created = categoryService.createCategory(dto);
        return ResponseEntity.ok(ApiResponse.success(created, "Thêm danh mục thành công!"));
    }

    // 2. Cập nhật danh mục
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO dto) {
        CategoryResponseDTO updated = categoryService.updateCategory(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật danh mục thành công!"));
    }

    // 3. Lấy chi tiết danh mục theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> getCategoryById(@PathVariable Long id) {
        CategoryResponseDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category, "Lấy thông tin danh mục thành công!"));
    }

    // 4. Lấy chi tiết danh mục theo Slug
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> getCategoryBySlug(@PathVariable String slug) {
        CategoryResponseDTO category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(category, "Lấy thông tin danh mục thành công!"));
    }

    // 5. Lấy danh sách toàn bộ danh mục (phẳng)
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getAllCategories() {
        List<CategoryResponseDTO> list = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(list, "Lấy danh sách danh mục thành công!"));
    }

    // 6. Lấy cây danh mục đa cấp (Laptop -> Laptop Gaming -> ASUS ROG)
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getCategoryTree() {
        List<CategoryResponseDTO> tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(ApiResponse.success(tree, "Lấy cây danh mục thành công!"));
    }

    // 7. Xóa danh mục
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa danh mục thành công!"));
    }
}
