package com.replay.admin;

import java.time.LocalDateTime;

public record UserAdminDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        String role,
        LocalDateTime createdAt
) {}
