package com.jeweleryshop.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jeweleryshop.backend.entity.Category;

/**
 * Repository dùng để thao tác với bảng 'categories'. Kế thừa JpaRepository cung
 * cấp đầy đủ CRUD + query mặc định.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Tìm danh mục theo tên.
     *
     * @param name Tên danh mục
     * @return Optional<Category>
     */
    Optional<Category> findByName(String name);
}
