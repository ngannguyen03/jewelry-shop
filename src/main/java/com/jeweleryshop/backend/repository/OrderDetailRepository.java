package com.jeweleryshop.backend.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jeweleryshop.backend.dto.DashboardStatsDTO.TopSellingProductDTO;
import com.jeweleryshop.backend.entity.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Query("SELECT new com.jeweleryshop.backend.dto.DashboardStatsDTO$TopSellingProductDTO(od.variant.id, od.variant.product.name, od.variant.name, SUM(od.quantity)) "
            + "FROM OrderDetail od JOIN od.order o "
            + "WHERE o.status = 'DELIVERED' "
            + "GROUP BY od.variant.id, od.variant.product.name, od.variant.name ORDER BY SUM(od.quantity) DESC")
    List<TopSellingProductDTO> findTopSellingProducts(Pageable pageable);
}
