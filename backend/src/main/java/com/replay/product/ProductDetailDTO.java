package com.replay.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDetailDTO(
        Long id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        String condition,
        String consoleType,
        int stockQuantity,
        String imageUrl,
        boolean archived,
        LocalDateTime createdAt,
        Long categoryId,
        String categoryName,
        String categorySlug
) {}
