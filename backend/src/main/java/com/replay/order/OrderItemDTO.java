package com.replay.order;

import java.math.BigDecimal;

public record OrderItemDTO(
        Long productId,
        String productName,
        String productSlug,
        String imageUrl,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
