package com.bookfair.genre.dto;

import java.util.List;

public record GenreRequest(
        Long eventId,
        List<String> genres
) {}
