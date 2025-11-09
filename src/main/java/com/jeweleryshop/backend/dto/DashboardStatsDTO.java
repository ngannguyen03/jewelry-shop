package com.jeweleryshop.backend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardStatsDTO {

    private BigDecimal totalRevenue;
    private BigDecimal revenueThisMonth;
    private BigDecimal revenueLast7Days;
    private Long totalOrders;
    private Long newUsersThisMonth;
    private Long newOrdersThisMonth;

    // Growth percentages
    private Double userGrowthPercentage;
    private Double orderGrowthPercentage;

    // Top selling products
    private List<TopSellingProductDTO> topSellingProducts;

    // 🆕 Product stats
    private Long totalProducts;
    private Long activeProducts;
    private Map<String, Long> productsByCategory;

    public DashboardStatsDTO() {
    }

    public DashboardStatsDTO(BigDecimal totalRevenue, BigDecimal revenueThisMonth, BigDecimal revenueLast7Days,
            Long totalOrders, Long newUsersThisMonth, Long newOrdersThisMonth,
            Double userGrowthPercentage, Double orderGrowthPercentage,
            List<TopSellingProductDTO> topSellingProducts) {
        this.totalRevenue = totalRevenue;
        this.revenueThisMonth = revenueThisMonth;
        this.revenueLast7Days = revenueLast7Days;
        this.totalOrders = totalOrders;
        this.newUsersThisMonth = newUsersThisMonth;
        this.newOrdersThisMonth = newOrdersThisMonth;
        this.userGrowthPercentage = userGrowthPercentage;
        this.orderGrowthPercentage = orderGrowthPercentage;
        this.topSellingProducts = topSellingProducts;
    }

    // === Getters / Setters ===
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getRevenueThisMonth() {
        return revenueThisMonth;
    }

    public void setRevenueThisMonth(BigDecimal revenueThisMonth) {
        this.revenueThisMonth = revenueThisMonth;
    }

    public BigDecimal getRevenueLast7Days() {
        return revenueLast7Days;
    }

    public void setRevenueLast7Days(BigDecimal revenueLast7Days) {
        this.revenueLast7Days = revenueLast7Days;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getNewUsersThisMonth() {
        return newUsersThisMonth;
    }

    public void setNewUsersThisMonth(Long newUsersThisMonth) {
        this.newUsersThisMonth = newUsersThisMonth;
    }

    public Long getNewOrdersThisMonth() {
        return newOrdersThisMonth;
    }

    public void setNewOrdersThisMonth(Long newOrdersThisMonth) {
        this.newOrdersThisMonth = newOrdersThisMonth;
    }

    public Double getUserGrowthPercentage() {
        return userGrowthPercentage;
    }

    public void setUserGrowthPercentage(Double userGrowthPercentage) {
        this.userGrowthPercentage = userGrowthPercentage;
    }

    public Double getOrderGrowthPercentage() {
        return orderGrowthPercentage;
    }

    public void setOrderGrowthPercentage(Double orderGrowthPercentage) {
        this.orderGrowthPercentage = orderGrowthPercentage;
    }

    public List<TopSellingProductDTO> getTopSellingProducts() {
        return topSellingProducts;
    }

    public void setTopSellingProducts(List<TopSellingProductDTO> topSellingProducts) {
        this.topSellingProducts = topSellingProducts;
    }

    public Long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public Long getActiveProducts() {
        return activeProducts;
    }

    public void setActiveProducts(Long activeProducts) {
        this.activeProducts = activeProducts;
    }

    public Map<String, Long> getProductsByCategory() {
        return productsByCategory;
    }

    public void setProductsByCategory(Map<String, Long> productsByCategory) {
        this.productsByCategory = productsByCategory;
    }

    // === Inner DTO ===
    public static class TopSellingProductDTO {

        private Long variantId;
        private String productName;
        private String variantName;
        private Long totalSold;

        public TopSellingProductDTO(Long variantId, String productName, String variantName, Long totalSold) {
            this.variantId = variantId;
            this.productName = productName;
            this.variantName = variantName;
            this.totalSold = totalSold;
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

        public String getVariantName() {
            return variantName;
        }

        public void setVariantName(String variantName) {
            this.variantName = variantName;
        }

        public Long getTotalSold() {
            return totalSold;
        }

        public void setTotalSold(Long totalSold) {
            this.totalSold = totalSold;
        }
    }
}
