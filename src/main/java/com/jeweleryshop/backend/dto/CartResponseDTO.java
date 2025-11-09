package com.jeweleryshop.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponseDTO {

    private Long id;
    private List<CartItemResponseDTO> items;
    private int totalItems;
    private BigDecimal subTotal; // Total before discount
    private BigDecimal discountAmount;
    private BigDecimal finalTotal; // Total after discount
    private String appliedPromotionCode;

    public CartResponseDTO() {
    }

    public CartResponseDTO(Long id, List<CartItemResponseDTO> items, int totalItems, BigDecimal subTotal, BigDecimal discountAmount, BigDecimal finalTotal, String appliedPromotionCode) {
        this.id = id;
        this.items = items;
        this.totalItems = totalItems;
        this.subTotal = subTotal;
        this.discountAmount = discountAmount;
        this.finalTotal = finalTotal;
        this.appliedPromotionCode = appliedPromotionCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<CartItemResponseDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponseDTO> items) {
        this.items = items;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getFinalTotal() {
        return finalTotal;
    }

    public void setFinalTotal(BigDecimal finalTotal) {
        this.finalTotal = finalTotal;
    }

    public String getAppliedPromotionCode() {
        return appliedPromotionCode;
    }

    public void setAppliedPromotionCode(String appliedPromotionCode) {
        this.appliedPromotionCode = appliedPromotionCode;
    }

    public static class CartItemResponseDTO {

        private Long cartItemId;
        private Long productVariantId;
        private String productName;
        private BigDecimal unitPrice; // Price of one unit of this variant
        private int quantity;
        private String imageUrl; // Main image of the product

        public CartItemResponseDTO() {
        }

        public CartItemResponseDTO(Long cartItemId, Long productVariantId, String productName, BigDecimal unitPrice, int quantity, String imageUrl) {
            this.cartItemId = cartItemId;
            this.productVariantId = productVariantId;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.imageUrl = imageUrl;
        }

        public Long getCartItemId() {
            return cartItemId;
        }

        public void setCartItemId(Long cartItemId) {
            this.cartItemId = cartItemId;
        }

        public Long getProductVariantId() {
            return productVariantId;
        }

        public void setProductVariantId(Long productVariantId) {
            this.productVariantId = productVariantId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}
