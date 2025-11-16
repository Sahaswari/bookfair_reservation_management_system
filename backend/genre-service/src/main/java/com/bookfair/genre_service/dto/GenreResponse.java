package com.bookfair.genre.dto;

public record GenreResponse(
        Long id,
        Long eventId,
        String genreName,
        String userSnapshot
) {}
