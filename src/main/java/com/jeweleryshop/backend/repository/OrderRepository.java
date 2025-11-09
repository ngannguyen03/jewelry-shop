package com.jeweleryshop.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jeweleryshop.backend.entity.Order;
import com.jeweleryshop.backend.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    long countByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED'")
    Optional<BigDecimal> findTotalRevenue();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED' AND o.orderDate >= :startDate")
    Optional<BigDecimal> findTotalRevenueSince(LocalDateTime startDate);

    // ✅ Custom query để load luôn chi tiết sản phẩm (orderDetails, variant, product)
    @Query("""
        SELECT DISTINCT o FROM Order o
        LEFT JOIN FETCH o.orderDetails od
        LEFT JOIN FETCH od.variant v
        LEFT JOIN FETCH v.product p
        WHERE o.id = :orderId
    """)
    Optional<Order> findByIdWithDetails(@Param("orderId") Long orderId);
}
