package com.bookfair.genre_service.service;

import com.bookfair.genre_service.dto.GenreRequest;
import com.bookfair.genre_service.dto.GenreResponse;

import java.util.List;
import java.util.UUID;

public interface GenreService {

    GenreResponse createGenre(GenreRequest req);

    List<GenreResponse> getAll();

    GenreResponse getById(UUID id);

    GenreResponse updateGenre(UUID id, GenreRequest req);

    void deleteGenre(UUID id);
}