package com.replay.admin;

public record AdminCategoryRequest(
        String name,
        String slug,
        String description,
        String imageUrl
) {}
