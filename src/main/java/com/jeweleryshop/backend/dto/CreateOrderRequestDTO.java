package com.jeweleryshop.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true) // ✅ Bỏ qua các field lạ để tránh lỗi parse JSON
public class CreateOrderRequestDTO {

    @NotNull(message = "Shipping Address ID cannot be null")
    private Long shippingAddressId;

    // ✅ Phương thức thanh toán
    private String paymentMethod;

    // ✅ Ghi chú đơn hàng
    private String notes;

    // ✅ Mã giảm giá (tùy chọn)
    private String discountCode;

    // ============================
    // 🔹 Getters & Setters
    // ============================
    public Long getShippingAddressId() {
        return shippingAddressId;
    }

    public void setShippingAddressId(Long shippingAddressId) {
        this.shippingAddressId = shippingAddressId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }
}
