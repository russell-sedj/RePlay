package com.replay.product;

import java.math.BigDecimal;

public record ProductSummaryDTO(
        Long id,
        String name,
        String slug,
        BigDecimal price,
        String condition,
        String consoleType,
        String imageUrl,
        int stockQuantity,
        String categoryName,
        String categorySlug
) {}
