package com.jeweleryshop.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.jeweleryshop.backend.dto.DashboardStatsDTO;
import com.jeweleryshop.backend.dto.DashboardStatsDTO.TopSellingProductDTO;
import com.jeweleryshop.backend.repository.OrderDetailRepository;
import com.jeweleryshop.backend.repository.OrderRepository;
import com.jeweleryshop.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderDetailRepository orderDetailRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private List<TopSellingProductDTO> mockTopProducts;

    @BeforeEach
    void setup() {
        // ✅ Tạo danh sách top sản phẩm đúng cấu trúc DTO thật
        TopSellingProductDTO p1 = new TopSellingProductDTO(1L, "Gold Ring", "Size 7", 10L);
        TopSellingProductDTO p2 = new TopSellingProductDTO(2L, "Silver Necklace", "Default", 8L);
        mockTopProducts = Arrays.asList(p1, p2);
    }

    // =====================================================
    // 🧩 MAIN DASHBOARD STATS
    // =====================================================
    @Test
    void testGetDashboardStats_Success() {
        when(orderRepository.findTotalRevenue()).thenReturn(Optional.of(new BigDecimal("10000000")));
        when(orderRepository.count()).thenReturn(50L);
        when(orderRepository.findTotalRevenueSince(any(LocalDateTime.class)))
                .thenReturn(Optional.of(new BigDecimal("3000000")));
        when(userRepository.countByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(10L)
                .thenReturn(5L); // Tháng trước
        when(orderRepository.countByOrderDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(20L)
                .thenReturn(10L);
        when(orderDetailRepository.findTopSellingProducts(PageRequest.of(0, 5))).thenReturn(mockTopProducts);

        DashboardStatsDTO dto = dashboardService.getDashboardStats();

        assertNotNull(dto);
        assertEquals(new BigDecimal("10000000"), dto.getTotalRevenue());
        assertTrue(dto.getUserGrowthPercentage() >= 0);
        assertTrue(dto.getOrderGrowthPercentage() >= 0);
        assertEquals(2, dto.getTopSellingProducts().size());

        verify(orderRepository, atLeastOnce()).findTotalRevenue();
        verify(orderDetailRepository).findTopSellingProducts(PageRequest.of(0, 5));
    }

    // =====================================================
    // 🧩 CALCULATE GROWTH LOGIC
    // =====================================================
    @Test
    void testCalculateGrowthPercentage_NormalCase() throws Exception {
        var method = DashboardService.class.getDeclaredMethod("calculateGrowthPercentage", long.class, long.class);
        method.setAccessible(true);

        double result = (double) method.invoke(dashboardService, 10L, 15L);
        assertEquals(50.0, result);
    }

    @Test
    void testCalculateGrowthPercentage_ZeroPreviousNonZeroCurrent() throws Exception {
        var method = DashboardService.class.getDeclaredMethod("calculateGrowthPercentage", long.class, long.class);
        method.setAccessible(true);

        double result = (double) method.invoke(dashboardService, 0L, 5L);
        assertEquals(100.0, result);
    }

    @Test
    void testCalculateGrowthPercentage_ZeroPreviousAndCurrent() throws Exception {
        var method = DashboardService.class.getDeclaredMethod("calculateGrowthPercentage", long.class, long.class);
        method.setAccessible(true);

        double result = (double) method.invoke(dashboardService, 0L, 0L);
        assertEquals(0.0, result);
    }

    @Test
    void testCalculateGrowthPercentage_NegativeGrowth() throws Exception {
        var method = DashboardService.class.getDeclaredMethod("calculateGrowthPercentage", long.class, long.class);
        method.setAccessible(true);

        double result = (double) method.invoke(dashboardService, 20L, 10L);
        assertEquals(-50.0, result);
    }
}
