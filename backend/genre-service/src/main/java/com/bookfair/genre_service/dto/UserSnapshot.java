package com.bookfair.genre.dto;

public record UserSnapshot(
        Long userId,
        String username,
        String email,
        String role
) {}
