package com.jeweleryshop.backend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 🧩 CategoryDTO ------------------------------------------------- DTO (Data
 * Transfer Object) dùng cho việc truyền dữ liệu danh mục giữa backend và
 * frontend. Bao gồm cả thông tin quan hệ danh mục cha và thời gian tạo/cập
 * nhật.
 */
public class CategoryDTO {

    private Long id;

    @NotBlank(message = "Category name cannot be blank")
    @Size(max = 100, message = "Category name must be less than 100 characters")
    private String name;

    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;

    // ✅ Danh mục cha (nếu có)
    private Long parentId;

    // ✅ Thời gian tạo và cập nhật
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ---------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------
    public CategoryDTO() {
    }

    public CategoryDTO(Long id, String name, String description, Long parentId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentId = parentId;
    }

    public CategoryDTO(Long id, String name, String description, Long parentId,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentId = parentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ---------------------------------------------------------
    // Getters & Setters (chuẩn Java naming)
    // ---------------------------------------------------------
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ---------------------------------------------------------
    // ✅ Alias getter/setter để tương thích với code/test cũ
    // ---------------------------------------------------------
    public Long getParent_id() {
        return parentId;
    }

    public void setParent_id(Long parent_id) {
        this.parentId = parent_id;
    }
}
