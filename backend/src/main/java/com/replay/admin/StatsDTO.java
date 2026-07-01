package com.replay.admin;

import java.math.BigDecimal;
import java.util.List;

public record StatsDTO(
        long totalUsers,
        long totalOrders,
        BigDecimal totalRevenue,
        long totalProducts,
        List<ProductSalesDTO> topProducts
) {}

record ProductSalesDTO(String name, String slug, long totalSold) {}
