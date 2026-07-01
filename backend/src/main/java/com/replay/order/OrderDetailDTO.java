package com.replay.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailDTO(
        Long id,
        LocalDateTime orderDate,
        String status,
        BigDecimal totalAmount,
        String transactionId,
        String shippingAddress,
        List<OrderItemDTO> items
) {}
