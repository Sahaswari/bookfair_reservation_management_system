package com.bookfair.genre.dto;

import java.util.UUID;

public record GenreRequest(
        UUID eventId,
        UUID userId,
        String genreName,
        String description
) {}
