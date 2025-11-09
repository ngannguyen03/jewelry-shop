package com.jeweleryshop.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.jeweleryshop.backend.dto.CategoryDTO;
import com.jeweleryshop.backend.service.CategoryService;

import jakarta.validation.Valid;

/**
 * 🧩 CategoryController ------------------------------------------------- Quản
 * lý các API liên quan đến danh mục sản phẩm: - Cho phép người dùng và AI
 * ChatBox lấy danh mục công khai - Cho phép Admin thực hiện CRUD danh mục
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // ✅ Cho phép React FE hoặc AI ChatBox gọi API
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // ======================================================
    // 🌍 1️⃣ PUBLIC API - Dành cho FE & AI ChatBox
    // ======================================================
    /**
     * ✅ API public (không cần đăng nhập) Trả về danh sách danh mục đơn giản,
     * tránh lỗi vòng lặp Hibernate. Dùng cho trang chủ hoặc AI Chat Box khi
     * hiển thị danh mục.
     */
    @GetMapping("/public/categories")
    public ResponseEntity<List<CategoryDTO>> getPublicCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // ======================================================
    // 👥 2️⃣ PUBLIC ENDPOINTS (Người dùng bình thường)
    // ======================================================
    /**
     * ✅ Lấy tất cả danh mục sản phẩm. Dành cho người dùng FE (không yêu cầu
     * đăng nhập)
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * ✅ Lấy chi tiết danh mục theo ID
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    // ======================================================
    // 🛠️ 3️⃣ ADMIN ENDPOINTS (Chỉ Admin)
    // ======================================================
    /**
     * ✅ Lấy danh sách tất cả danh mục cho Admin (Dashboard)
     */
    @GetMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesForAdmin() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * ✅ Tạo mới danh mục
     */
    @PostMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO createdCategory = categoryService.createCategory(categoryDTO);
        return ResponseEntity
                .created(URI.create("/api/categories/" + createdCategory.getId()))
                .body(createdCategory);
    }

    /**
     * ✅ Cập nhật thông tin danh mục
     */
    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO
    ) {
        CategoryDTO updatedCategory = categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * ✅ Xóa danh mục theo ID
     */
    @DeleteMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build(); // HTTP 204
    }
}
