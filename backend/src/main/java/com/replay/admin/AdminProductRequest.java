package com.replay.admin;

import com.replay.product.ConsoleType;
import com.replay.product.ProductCondition;
import java.math.BigDecimal;

public record AdminProductRequest(
        String name,
        String slug,
        String description,
        BigDecimal price,
        ProductCondition condition,
        ConsoleType consoleType,
        int stockQuantity,
        String imageUrl,
        Long categoryId,
        boolean archived
) {}
