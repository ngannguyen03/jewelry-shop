package com.jeweleryshop.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.jeweleryshop.backend.entity.Product;

public class ProductResponseDTO {

    private Long id;
    private String name;
    private String description;

    private BigDecimal basePrice;      // 💰 Giá gốc
    private BigDecimal discountPrice;  // 💸 Giá giảm
    private String skuPrefix;          // 🔖 Mã sản phẩm (SKU)
    private Boolean isActive;          // ✅ Trạng thái hoạt động

    // ✅ THÊM: Field imageUrl cho Cloudinary
    private String imageUrl;

    private Long categoryId;
    private String categoryName;

    // ✅ Thêm đối tượng CategoryDTO cho HATEOAS
    private CategoryDTO category;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ProductImageDTO> images;
    private List<ProductVariantDTO> variants;

    // =========================
    // 🔹 Constructors
    // =========================
    public ProductResponseDTO() {
    }

    public ProductResponseDTO(
            Long id,
            String name,
            String description,
            BigDecimal basePrice,
            BigDecimal discountPrice,
            String skuPrefix,
            Boolean isActive,
            String imageUrl, // ✅ THÊM: imageUrl parameter
            Long categoryId,
            String categoryName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<ProductImageDTO> images,
            List<ProductVariantDTO> variants
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.discountPrice = discountPrice;
        this.skuPrefix = skuPrefix;
        this.isActive = isActive;
        this.imageUrl = imageUrl; // ✅ THÊM: Set imageUrl
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.images = images;
        this.variants = variants;
    }

    // ✅ Constructor mapping từ Entity Product
    public ProductResponseDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.basePrice = product.getBasePrice();
        this.discountPrice = product.getDiscountPrice();
        this.skuPrefix = product.getSkuPrefix();
        this.isActive = product.getIsActive();
        this.imageUrl = product.getImageUrl(); // ✅ THÊM: Lấy imageUrl từ entity
        this.categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        this.categoryName = product.getCategory() != null ? product.getCategory().getName() : null;
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();

        this.images = product.getImages().stream()
                .map(img -> new ProductImageDTO(img.getId(), img.getImageUrl()))
                .collect(Collectors.toList());

        this.variants = product.getVariants().stream()
                .map(ProductVariantDTO::new)
                .collect(Collectors.toList());
    }

    // =========================
    // 🔹 Getters & Setters
    // =========================
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

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(BigDecimal discountPrice) {
        this.discountPrice = discountPrice;
    }

    public String getSkuPrefix() {
        return skuPrefix;
    }

    public void setSkuPrefix(String skuPrefix) {
        this.skuPrefix = skuPrefix;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // ✅ THÊM: Getter và Setter cho imageUrl
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public CategoryDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryDTO category) {
        this.category = category;
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

    public List<ProductImageDTO> getImages() {
        return images;
    }

    public void setImages(List<ProductImageDTO> images) {
        this.images = images;
    }

    public List<ProductVariantDTO> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariantDTO> variants) {
        this.variants = variants;
    }
}
