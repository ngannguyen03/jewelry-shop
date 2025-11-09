package com.jeweleryshop.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeweleryshop.backend.dto.CategoryDTO;
import com.jeweleryshop.backend.entity.Category;
import com.jeweleryshop.backend.exception.DuplicateResourceException;
import com.jeweleryshop.backend.exception.ResourceNotFoundException;
import com.jeweleryshop.backend.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // =====================================================
    // 📖 LẤY DANH SÁCH DANH MỤC (Public / Admin)
    // =====================================================
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // 🌍 LẤY DANH SÁCH DANH MỤC CHO FE / AI CHATBOX (PUBLIC)
    // =====================================================
    @Transactional(readOnly = true)
    public List<CategoryDTO> getPublicCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(category -> {
                    CategoryDTO dto = new CategoryDTO();
                    dto.setId(category.getId());
                    dto.setName(category.getName());
                    dto.setDescription(category.getDescription());
                    dto.setParentId(category.getParent() != null ? category.getParent().getId() : null);
                    dto.setCreatedAt(category.getCreatedAt());
                    dto.setUpdatedAt(category.getUpdatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // =====================================================
    // 🔍 LẤY DANH MỤC THEO ID
    // =====================================================
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(()
                        -> new ResourceNotFoundException("Category not found with id: " + id));
        return convertToDTO(category);
    }

    // =====================================================
    // ➕ TẠO DANH MỤC MỚI (Admin)
    // =====================================================
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        // 🔸 Kiểm tra trùng tên
        categoryRepository.findByName(categoryDTO.getName()).ifPresent(existing -> {
            throw new DuplicateResourceException(
                    "Category with name '" + categoryDTO.getName() + "' already exists."
            );
        });

        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());

        // ✅ Gắn danh mục cha nếu có
        if (categoryDTO.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(()
                            -> new ResourceNotFoundException(
                            "Parent category not found with id: " + categoryDTO.getParentId()
                    ));
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    // =====================================================
    // ✏️ CẬP NHẬT DANH MỤC (Admin)
    // =====================================================
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(()
                        -> new ResourceNotFoundException("Category not found with id: " + id));

        // 🔸 Kiểm tra trùng tên
        categoryRepository.findByName(categoryDTO.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException(
                        "Category with name '" + categoryDTO.getName() + "' already exists."
                );
            }
        });

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());

        // ✅ Cập nhật danh mục cha (nếu có)
        if (categoryDTO.getParentId() != null) {
            Category parent = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(()
                            -> new ResourceNotFoundException(
                            "Parent category not found with id: " + categoryDTO.getParentId()
                    ));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category updated = categoryRepository.save(category);
        return convertToDTO(updated);
    }

    // =====================================================
    // 🗑️ XÓA DANH MỤC (Admin)
    // =====================================================
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(()
                        -> new ResourceNotFoundException("Category not found with id: " + id));
        categoryRepository.delete(category);
    }

    // =====================================================
    // 🧩 HÀM CHUYỂN ENTITY -> DTO
    // =====================================================
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setParentId(category.getParent() != null ? category.getParent().getId() : null);
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }
}
