package com.jeweleryshop.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jeweleryshop.backend.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // ✅ Dùng EntityGraph để tránh lỗi N+1 khi lấy danh sách sản phẩm
    @Override
    @EntityGraph(attributePaths = {"category", "images", "variants", "variants.inventory"})
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    // ✅ Lấy chi tiết sản phẩm kèm các quan hệ (category, images, variants)
    @Override
    @EntityGraph(attributePaths = {"category", "images", "variants", "variants.inventory"})
    Optional<Product> findById(Long id);

    // 🆕 Thêm phương thức cho Excel import
    Optional<Product> findByName(String name);

    // Các phương thức tìm kiếm khác (nếu có)
    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByIsActiveTrue();

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword% OR p.skuPrefix LIKE %:keyword%")
    List<Product> searchProducts(@Param("keyword") String keyword);
}
