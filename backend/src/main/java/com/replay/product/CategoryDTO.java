package com.replay.product;

public record CategoryDTO(
        Long id,
        String name,
        String slug,
        String description,
        String imageUrl
) {}
