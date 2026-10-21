package com.example.catalogservice.repository;

import com.example.catalogservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    // Lấy tất cả danh mục gốc cấp cao nhất (parent_id is null)
    List<Category> findByParentIsNull();

    // Lấy danh mục con theo ID danh mục cha
    List<Category> findByParentId(Long parentId);
}
