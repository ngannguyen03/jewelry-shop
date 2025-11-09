package com.jeweleryshop.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jeweleryshop.backend.dto.OrderDetailResponseDTO;
import com.jeweleryshop.backend.dto.UpdateOrderStatusRequestDTO;
import com.jeweleryshop.backend.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Get a paginated list of all orders. Example:
     * /api/admin/orders?page=0&size=10&sort=orderDate,desc
     */
    @GetMapping
    public ResponseEntity<Page<OrderDetailResponseDTO>> getAllOrders(Pageable pageable) {
        Page<OrderDetailResponseDTO> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Update the status of a specific order.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDetailResponseDTO> updateOrderStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequestDTO requestDTO) {
        OrderDetailResponseDTO updatedOrder = orderService.updateOrderStatus(id, requestDTO.getStatus());
        return ResponseEntity.ok(updatedOrder);
    }
}
