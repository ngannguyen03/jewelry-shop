package com.jeweleryshop.backend.dto;

import java.math.BigDecimal;

import com.jeweleryshop.backend.entity.ProductVariant;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductVariantDTO {

    private Long id;
    private String size;
    private String material;
    private BigDecimal priceModifier;

    @NotBlank(message = "SKU cannot be blank")
    private String sku;

    @NotNull
    @Min(0)
    private Integer quantity;

    public ProductVariantDTO() {
    }

    public ProductVariantDTO(Long id, String size, String material, BigDecimal priceModifier, String sku, Integer quantity) {
        this.id = id;
        this.size = size;
        this.material = material;
        this.priceModifier = priceModifier;
        this.sku = sku;
        this.quantity = quantity;
    }

    // ✅ Constructor nhận từ entity ProductVariant
    public ProductVariantDTO(ProductVariant variant) {
        this.id = variant.getId();
        this.size = variant.getSize();
        this.material = variant.getMaterial();
        this.priceModifier = variant.getPriceModifier();
        this.sku = variant.getSku();
        this.quantity = variant.getInventory() != null ? variant.getInventory().getQuantity() : 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public BigDecimal getPriceModifier() {
        return priceModifier;
    }

    public void setPriceModifier(BigDecimal priceModifier) {
        this.priceModifier = priceModifier;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
