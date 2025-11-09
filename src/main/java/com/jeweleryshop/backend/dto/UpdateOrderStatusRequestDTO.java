package com.jeweleryshop.backend.dto;

import com.jeweleryshop.backend.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateOrderStatusRequestDTO {

    @NotNull(message = "Order status cannot be null")
    private OrderStatus status;

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
