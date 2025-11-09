package com.jeweleryshop.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jeweleryshop.backend.dto.CreateOrderRequestDTO;
import com.jeweleryshop.backend.dto.OrderDetailResponseDTO;
import com.jeweleryshop.backend.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@PreAuthorize("hasRole('USER')")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDetailResponseDTO> createOrder(@Valid @RequestBody CreateOrderRequestDTO requestDTO) {
        OrderDetailResponseDTO createdOrder = orderService.createOrder(requestDTO);
        return ResponseEntity.created(URI.create("/api/orders/" + createdOrder.getId())).body(createdOrder);
    }

    @GetMapping
    public ResponseEntity<List<OrderDetailResponseDTO>> getOrderHistory() {
        List<OrderDetailResponseDTO> orders = orderService.getOrdersForCurrentUser();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponseDTO> getOrderDetails(@PathVariable Long id) {
        OrderDetailResponseDTO orderDetails = orderService.getOrderDetails(id);
        return ResponseEntity.ok(orderDetails);
    }
}
