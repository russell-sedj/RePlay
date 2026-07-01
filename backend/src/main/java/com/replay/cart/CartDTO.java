package com.replay.cart;

import java.math.BigDecimal;
import java.util.List;

public record CartDTO(
        Long id,
        List<CartItemDTO> items,
        BigDecimal total,
        boolean isEmpty
) {}
