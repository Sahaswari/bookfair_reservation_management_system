package com.bookfair.genre.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record GenreResponse(
        UUID id,
        UUID eventId,
        UUID userId,
        String genreName,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Object user
) {}

