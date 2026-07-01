package com.replay.auth;

public record UserProfileDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        String role
) {}
