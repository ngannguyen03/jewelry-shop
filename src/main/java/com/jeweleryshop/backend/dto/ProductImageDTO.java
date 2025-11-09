package com.jeweleryshop.backend.dto;

import com.jeweleryshop.backend.entity.ProductImage;

public class ProductImageDTO {

    private Long id;
    private String imageUrl;

    public ProductImageDTO() {
    }

    public ProductImageDTO(Long id, String imageUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
    }

    // ✅ Constructor nhận từ entity ProductImage
    public ProductImageDTO(ProductImage image) {
        this.id = image.getId();
        this.imageUrl = image.getImageUrl();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
