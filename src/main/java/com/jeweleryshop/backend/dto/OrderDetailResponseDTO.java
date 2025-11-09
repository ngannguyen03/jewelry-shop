package com.jeweleryshop.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jeweleryshop.backend.entity.OrderStatus;

/**
 * ✅ DTO trả thông tin chi tiết đơn hàng cho cả user & admin Bao gồm: thông tin
 * cơ bản, khách hàng, địa chỉ, danh sách sản phẩm
 */
public class OrderDetailResponseDTO {

    private Long id;
    private LocalDateTime orderDate;
    private OrderStatus status;

    // 🧾 Tổng phụ, giảm giá, tổng cuối
    @JsonProperty("subTotal")
    private BigDecimal subTotal;

    @JsonProperty("discountAmount")
    private BigDecimal discountAmount;

    @JsonProperty("finalTotal")
    private BigDecimal finalTotal;

    // 🏷️ Mã giảm giá
    @JsonProperty("discountCode")
    private String discountCode;

    // 🚚 Phí vận chuyển
    @JsonProperty("shippingFee")
    private BigDecimal shippingFee;

    // 🧍‍♂️ Thông tin khách hàng
    @JsonProperty("userEmail")
    private String userEmail;

    @JsonProperty("userName")
    private String userName;

    // 💰 Tổng tiền chính (hiển thị ở admin)
    @JsonProperty("totalPrice")
    private BigDecimal totalAmount;

    // 📦 Địa chỉ giao hàng
    @JsonProperty("shippingAddress")
    private AddressDTO shippingAddress;

    // 🛍️ Danh sách sản phẩm trong đơn
    @JsonProperty("orderItems")
    private List<OrderItemDTO> orderItems;

    // ===================== CONSTRUCTORS =====================
    public OrderDetailResponseDTO() {
    }

    public OrderDetailResponseDTO(
            Long id,
            LocalDateTime orderDate,
            OrderStatus status,
            BigDecimal subTotal,
            BigDecimal discountAmount,
            BigDecimal finalTotal,
            AddressDTO shippingAddress,
            List<OrderItemDTO> orderItems
    ) {
        this.id = id;
        this.orderDate = orderDate;
        this.status = status;
        this.subTotal = subTotal;
        this.discountAmount = discountAmount;
        this.finalTotal = finalTotal;
        this.shippingAddress = shippingAddress;
        this.orderItems = orderItems;
    }

    // ===================== GETTERS & SETTERS =====================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
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

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigDecimal getTotalPrice() {
        return totalAmount;
    }

    public void setTotalPrice(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public AddressDTO getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(AddressDTO shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<OrderItemDTO> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemDTO> orderItems) {
        this.orderItems = orderItems;
    }

    // ===================== NESTED CLASS =====================
    /**
     * ✅ Đại diện cho từng sản phẩm trong đơn hàng
     */
    public static class OrderItemDTO {

        private Long productId;
        private Long variantId;
        private String productName;
        private String variantInfo;
        private String imageUrl;
        private int quantity;

        // 💰 Giá tại thời điểm mua
        @JsonProperty("priceAtPurchase")
        private BigDecimal priceAtPurchase;

        public OrderItemDTO() {
        }

        public OrderItemDTO(
                Long productId,
                Long variantId,
                String productName,
                String variantInfo,
                String imageUrl,
                int quantity,
                BigDecimal priceAtPurchase
        ) {
            this.productId = productId;
            this.variantId = variantId;
            this.productName = productName;
            this.variantInfo = variantInfo;
            this.imageUrl = imageUrl;
            this.quantity = quantity;
            this.priceAtPurchase = priceAtPurchase;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Long getVariantId() {
            return variantId;
        }

        public void setVariantId(Long variantId) {
            this.variantId = variantId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getVariantInfo() {
            return variantInfo;
        }

        public void setVariantInfo(String variantInfo) {
            this.variantInfo = variantInfo;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPriceAtPurchase() {
            return priceAtPurchase;
        }

        public void setPriceAtPurchase(BigDecimal priceAtPurchase) {
            this.priceAtPurchase = priceAtPurchase;
        }
    }
}
