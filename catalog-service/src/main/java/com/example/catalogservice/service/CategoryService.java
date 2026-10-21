package com.example.catalogservice.service;

import com.example.catalogservice.dto.request.CategoryRequestDTO;
import com.example.catalogservice.dto.response.CategoryResponseDTO;
import com.example.catalogservice.entity.Category;
import com.example.catalogservice.exception.AppException;
import com.example.catalogservice.exception.ResourceNotFoundException;
import com.example.catalogservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 1. Tạo mới danh mục
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new AppException(400, "Tên danh mục đã tồn tại!");
        }

        String slug = (dto.getSlug() != null && !dto.getSlug().isBlank())
                ? dto.getSlug()
                : toSlug(dto.getName());

        if (categoryRepository.existsBySlug(slug)) {
            throw new AppException(400, "Slug danh mục đã tồn tại: " + slug);
        }

        Category parent = null;
        if (dto.getParentId() != null) {
            parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục cha với ID: " + dto.getParentId()));
        }

        Category category = Category.builder()
                .name(dto.getName())
                .slug(slug)
                .description(dto.getDescription())
                .parent(parent)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToDTO(saved);
    }

    // 2. Cập nhật danh mục
    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));

        // Kiểm tra trùng lặp tên nếu đổi tên
        if (!category.getName().equalsIgnoreCase(dto.getName()) && categoryRepository.existsByName(dto.getName())) {
            throw new AppException(400, "Tên danh mục đã tồn tại!");
        }

        String slug = (dto.getSlug() != null && !dto.getSlug().isBlank())
                ? dto.getSlug()
                : toSlug(dto.getName());

        if (!category.getSlug().equalsIgnoreCase(slug) && categoryRepository.existsBySlug(slug)) {
            throw new AppException(400, "Slug danh mục đã tồn tại: " + slug);
        }

        Category parent = null;
        if (dto.getParentId() != null) {
            if (dto.getParentId().equals(id)) {
                throw new AppException(400, "Danh mục không thể làm cha của chính nó!");
            }
            parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục cha với ID: " + dto.getParentId()));
        }

        category.setName(dto.getName());
        category.setSlug(slug);
        category.setDescription(dto.getDescription());
        category.setParent(parent);

        Category updated = categoryRepository.save(category);
        return mapToDTO(updated);
    }

    // 3. Lấy chi tiết danh mục theo ID
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
        return mapToDTO(category);
    }

    // 4. Lấy chi tiết theo Slug
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với slug: " + slug));
        return mapToDTO(category);
    }

    // 5. Lấy danh sách toàn bộ danh mục (phẳng)
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 6. Lấy cây danh mục đa cấp (VD: Laptop -> Laptop Gaming -> ASUS ROG)
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getCategoryTree() {
        // Lấy danh mục gốc cấp cao nhất (parent is null)
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        return rootCategories.stream()
                .map(this::mapToTreeDTO)
                .collect(Collectors.toList());
    }

    // 7. Xóa danh mục
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
        categoryRepository.delete(category);
    }

    // Hàm chuyển đổi sang DTO đơn lẻ
    private CategoryResponseDTO mapToDTO(Category c) {
        return CategoryResponseDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .parentName(c.getParent() != null ? c.getParent().getName() : null)
                .build();
    }

    // Hàm chuyển đổi đệ quy cây đa cấp
    private CategoryResponseDTO mapToTreeDTO(Category c) {
        List<CategoryResponseDTO> subs = null;
        if (c.getSubCategories() != null && !c.getSubCategories().isEmpty()) {
            subs = c.getSubCategories().stream()
                    .map(this::mapToTreeDTO)
                    .collect(Collectors.toList());
        }

        return CategoryResponseDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .parentName(c.getParent() != null ? c.getParent().getName() : null)
                .subCategories(subs)
                .build();
    }

    // Helper tạo slug từ tên
    private String toSlug(String input) {
        if (input == null) return "";
        String nowhitespace = Pattern.compile("[\\s]").matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
