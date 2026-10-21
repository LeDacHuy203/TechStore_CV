package com.example.catalogservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.catalogservice.dto.request.ProductCreateDTO;
import com.example.catalogservice.dto.request.ProductUpdateDTO;
import com.example.catalogservice.dto.response.ProductResponseDTO;
import com.example.catalogservice.entity.Brand;
import com.example.catalogservice.entity.Category;
import com.example.catalogservice.entity.Product;
import com.example.catalogservice.entity.ProductSpect;
import com.example.catalogservice.exception.AppException;
import com.example.catalogservice.exception.ResourceNotFoundException;
import com.example.catalogservice.repository.BrandRepository;
import com.example.catalogservice.repository.CategoryRepository;
import com.example.catalogservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final Cloudinary cloudinary;

    // 1. Thêm mới sản phẩm kèm upload ảnh lên Cloudinary
    @Transactional
    public ProductResponseDTO createProduct(ProductCreateDTO dto) throws IOException {
        // Kiểm tra SKU trùng lặp
        if (productRepository.existsBySku(dto.getSku())) {
            throw new AppException(400, "Mã SKU sản phẩm đã tồn tại!");
        }

        // Kiểm tra danh mục
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục!"));

        // Kiểm tra thương hiệu (nếu có)
        Brand brand = null;
        if (dto.getBrandId() != null) {
            brand = brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu!"));
        }

        // Upload ảnh lên Cloudinary nếu có file
        String imageUrl = null;
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    dto.getImageFile().getBytes(),
                    ObjectUtils.asMap("folder", "techstore/products")
            );
            imageUrl = uploadResult.get("secure_url").toString();
        }

        // Khởi tạo Product
        Product product = Product.builder()
                .sku(dto.getSku())
                .name(dto.getName())
                .basePrice(dto.getBasePrice())
                .discountPrice(dto.getDiscountPrice())
                .description(dto.getDescription())
                .primaryImageUrl(imageUrl)
                .category(category)
                .brand(brand)
                .published(true)
                .build();

        // Lưu thông số kỹ thuật (CPU, RAM, GPU...)
        if (dto.getSpecifications() != null && !dto.getSpecifications().isEmpty()) {
            List<ProductSpect> specs = new ArrayList<>();
            for (Map.Entry<String, String> entry : dto.getSpecifications().entrySet()) {
                specs.add(ProductSpect.builder()
                        .product(product)
                        .specKey(entry.getKey())
                        .specValue(entry.getValue())
                        .build());
            }
            product.setSpecs(specs);
        }

        Product saved = productRepository.save(product);
        return mapToDTO(saved);
    }

    // 2. Cập nhật sản phẩm & thay thế ảnh Cloudinary
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductUpdateDTO dto) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        // Cập nhật danh mục
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục!"));
        product.setCategory(category);

        // Cập nhật thương hiệu (nếu có)
        if (dto.getBrandId() != null) {
            Brand brand = brandRepository.findById(dto.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu!"));
            product.setBrand(brand);
        } else {
            product.setBrand(null);
        }

        // Xử lý ảnh: Nếu có upload ảnh mới
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            // Xóa ảnh cũ trên Cloudinary nếu có
            if (product.getPrimaryImageUrl() != null) {
                deleteCloudinaryImage(product.getPrimaryImageUrl());
            }

            // Upload ảnh mới
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    dto.getImageFile().getBytes(),
                    ObjectUtils.asMap("folder", "techstore/products")
            );
            product.setPrimaryImageUrl(uploadResult.get("secure_url").toString());
        }

        // Cập nhật thông tin cơ bản
        product.setName(dto.getName());
        product.setBasePrice(dto.getBasePrice());
        product.setDiscountPrice(dto.getDiscountPrice());
        product.setDescription(dto.getDescription());
        if (dto.getPublished() != null) {
            product.setPublished(dto.getPublished());
        }

        // Cập nhật thông số kỹ thuật (Specs)
        if (dto.getSpecifications() != null) {
            if (product.getSpecs() == null) {
                product.setSpecs(new ArrayList<>());
            } else {
                product.getSpecs().clear();
            }
            for (Map.Entry<String, String> entry : dto.getSpecifications().entrySet()) {
                product.getSpecs().add(ProductSpect.builder()
                        .product(product)
                        .specKey(entry.getKey())
                        .specValue(entry.getValue())
                        .build());
            }
        }

        Product updated = productRepository.save(product);
        return mapToDTO(updated);
    }

    // 3. Lấy chi tiết sản phẩm theo ID
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        return mapToDTO(product);
    }

    // 4. Lấy chi tiết sản phẩm theo SKU
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với mã SKU: " + sku));
        return mapToDTO(product);
    }

    // 5. Tìm kiếm & Lọc phân trang động
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProducts(
            Long categoryId,
            Long brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword,
            Boolean published,
            Pageable pageable) {

        Page<Product> productPage = productRepository.filterProducts(
                categoryId, brandId, minPrice, maxPrice, keyword, published, pageable);

        return productPage.map(this::mapToDTO);
    }

    // 6. Xóa sản phẩm
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        // Xóa ảnh trên Cloudinary
        if (product.getPrimaryImageUrl() != null) {
            deleteCloudinaryImage(product.getPrimaryImageUrl());
        }

        productRepository.delete(product);
    }

    // Hàm chuyển đổi từ Entity sang DTO
    public ProductResponseDTO mapToDTO(Product p) {
        Map<String, String> specMap = new HashMap<>();
        if (p.getSpecs() != null) {
            p.getSpecs().forEach(s -> specMap.put(s.getSpecKey(), s.getSpecValue()));
        }

        List<String> imageUrls = new ArrayList<>();
        if (p.getImages() != null) {
            imageUrls = p.getImages().stream()
                    .map(img -> img.getImageUrl())
                    .collect(Collectors.toList());
        }

        return ProductResponseDTO.builder()
                .id(p.getId())
                .sku(p.getSku())
                .name(p.getName())
                .basePrice(p.getBasePrice())
                .discountPrice(p.getDiscountPrice())
                .description(p.getDescription())
                .primaryImageUrl(p.getPrimaryImageUrl())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .brandId(p.getBrand() != null ? p.getBrand().getId() : null)
                .brandName(p.getBrand() != null ? p.getBrand().getName() : null)
                .published(p.isPublished())
                .specifications(specMap)
                .imageUrls(imageUrls)
                .createdAt(p.getCreatedAt())
                .build();
    }

    // Hàm phụ trợ xóa ảnh trên Cloudinary
    private void deleteCloudinaryImage(String imageUrl) {
        try {
            String[] parts = imageUrl.split("/techstore/products/");
            if (parts.length > 1) {
                String fileNameWithExt = parts[1];
                int dotIdx = fileNameWithExt.lastIndexOf('.');
                String fileName = dotIdx > 0 ? fileNameWithExt.substring(0, dotIdx) : fileNameWithExt;
                String publicId = "techstore/products/" + fileName;
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception e) {
            System.err.println("Không thể xóa ảnh cũ trên Cloudinary: " + e.getMessage());
        }
    }
}
