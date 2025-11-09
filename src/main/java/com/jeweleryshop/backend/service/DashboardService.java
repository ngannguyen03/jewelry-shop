package com.jeweleryshop.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jeweleryshop.backend.dto.DashboardStatsDTO;
import com.jeweleryshop.backend.dto.DashboardStatsDTO.TopSellingProductDTO;
import com.jeweleryshop.backend.entity.Category;
import com.jeweleryshop.backend.repository.CategoryRepository;
import com.jeweleryshop.backend.repository.OrderDetailRepository;
import com.jeweleryshop.backend.repository.OrderRepository;
import com.jeweleryshop.backend.repository.ProductRepository;
import com.jeweleryshop.backend.repository.UserRepository;

@Service
public class DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public DashboardService(OrderRepository orderRepository,
            UserRepository userRepository,
            OrderDetailRepository orderDetailRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        // --- Time Ranges ---
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfMonth.minusNanos(1);
        LocalDateTime startOfLast7Days = now.minusDays(7);

        // --- Core Stats ---
        BigDecimal totalRevenue = orderRepository.findTotalRevenue().orElse(BigDecimal.ZERO);
        long totalOrders = orderRepository.count();
        BigDecimal revenueThisMonth = orderRepository.findTotalRevenueSince(startOfMonth).orElse(BigDecimal.ZERO);
        BigDecimal revenueLast7Days = orderRepository.findTotalRevenueSince(startOfLast7Days).orElse(BigDecimal.ZERO);

        // --- Monthly Stats ---
        long newUsersThisMonth = userRepository.countByCreatedAtBetween(startOfMonth, now);
        long newOrdersThisMonth = orderRepository.countByOrderDateBetween(startOfMonth, now);

        // --- Growth Calculation ---
        long newUsersLastMonth = userRepository.countByCreatedAtBetween(startOfLastMonth, endOfLastMonth);
        long newOrdersLastMonth = orderRepository.countByOrderDateBetween(startOfLastMonth, endOfLastMonth);

        Double userGrowth = calculateGrowthPercentage(newUsersLastMonth, newUsersThisMonth);
        Double orderGrowth = calculateGrowthPercentage(newOrdersLastMonth, newOrdersThisMonth);

        // --- Top Selling Products ---
        List<TopSellingProductDTO> topSellingProducts
                = orderDetailRepository.findTopSellingProducts(PageRequest.of(0, 5));

        // --- Product Statistics ---
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.findByIsActiveTrue().size();

        Map<String, Long> productsByCategory = new HashMap<>();
        for (Category category : categoryRepository.findAll()) {
            long count = category.getProducts() != null ? category.getProducts().size() : 0;
            productsByCategory.put(category.getName(), count);
        }

        // --- Build DTO ---
        DashboardStatsDTO dto = new DashboardStatsDTO(
                totalRevenue,
                revenueThisMonth,
                revenueLast7Days,
                totalOrders,
                newUsersThisMonth,
                newOrdersThisMonth,
                userGrowth,
                orderGrowth,
                topSellingProducts
        );

        dto.setTotalProducts(totalProducts);
        dto.setActiveProducts(activeProducts);
        dto.setProductsByCategory(productsByCategory);

        return dto;
    }

    private Double calculateGrowthPercentage(long previous, long current) {
        if (previous == 0) {
            return (current > 0) ? 100.0 : 0.0;
        }
        double percentage = ((double) (current - previous) / previous) * 100;
        return new BigDecimal(percentage).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
