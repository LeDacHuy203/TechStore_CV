package com.example.catalogservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.catalogservice.dto.request.BrandRequestDTO;
import com.example.catalogservice.dto.response.BrandResponseDTO;
import com.example.catalogservice.entity.Brand;
import com.example.catalogservice.exception.AppException;
import com.example.catalogservice.exception.ResourceNotFoundException;
import com.example.catalogservice.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final Cloudinary cloudinary;

    // 1. Tạo thương hiệu mới (hỗ trợ upload logo lên Cloudinary)
    @Transactional
    public BrandResponseDTO createBrand(BrandRequestDTO dto) throws IOException {
        if (brandRepository.existsByName(dto.getName())) {
            throw new AppException(400, "Thương hiệu đã tồn tại trong hệ thống!");
        }

        String logoUrl = dto.getLogoUrl();
        if (dto.getLogoFile() != null && !dto.getLogoFile().isEmpty()) {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    dto.getLogoFile().getBytes(),
                    ObjectUtils.asMap("folder", "techstore/brands")
            );
            logoUrl = uploadResult.get("secure_url").toString();
        }

        Brand brand = Brand.builder()
                .name(dto.getName())
                .logoUrl(logoUrl)
                .build();

        Brand saved = brandRepository.save(brand);
        return mapToDTO(saved);
    }

    // 2. Cập nhật thương hiệu
    @Transactional
    public BrandResponseDTO updateBrand(Long id, BrandRequestDTO dto) throws IOException {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id));

        if (!brand.getName().equalsIgnoreCase(dto.getName()) && brandRepository.existsByName(dto.getName())) {
            throw new AppException(400, "Tên thương hiệu đã tồn tại!");
        }

        brand.setName(dto.getName());

        if (dto.getLogoFile() != null && !dto.getLogoFile().isEmpty()) {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    dto.getLogoFile().getBytes(),
                    ObjectUtils.asMap("folder", "techstore/brands")
            );
            brand.setLogoUrl(uploadResult.get("secure_url").toString());
        } else if (dto.getLogoUrl() != null) {
            brand.setLogoUrl(dto.getLogoUrl());
        }

        Brand updated = brandRepository.save(brand);
        return mapToDTO(updated);
    }

    // 3. Lấy chi tiết thương hiệu
    @Transactional(readOnly = true)
    public BrandResponseDTO getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id));
        return mapToDTO(brand);
    }

    // 4. Lấy tất cả thương hiệu
    @Transactional(readOnly = true)
    public List<BrandResponseDTO> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 5. Xóa thương hiệu
    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thương hiệu với ID: " + id));
        brandRepository.delete(brand);
    }

    private BrandResponseDTO mapToDTO(Brand b) {
        return BrandResponseDTO.builder()
                .id(b.getId())
                .name(b.getName())
                .logoUrl(b.getLogoUrl())
                .build();
    }
}
