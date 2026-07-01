package com.replay.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderAdminDTO(
        Long id,
        Long userId,
        String userEmail,
        LocalDateTime orderDate,
        String status,
        BigDecimal totalAmount,
        String transactionId,
        String shippingAddress
) {}
