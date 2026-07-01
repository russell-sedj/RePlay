package com.replay.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserProfileDTO user
) {}
