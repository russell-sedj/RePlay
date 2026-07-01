package com.replay.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryDTO(
        Long id,
        LocalDateTime orderDate,
        String status,
        BigDecimal totalAmount,
        String transactionId,
        int itemCount
) {}
