package com.bookfair.genre.service;

import com.bookfair.genre.dto.GenreRequest;
import com.bookfair.genre.dto.GenreResponse;

import java.util.List;
import java.util.UUID;

public interface GenreService {

    GenreResponse createGenre(GenreRequest request);

    List<GenreResponse> getAllGenres();

    GenreResponse getById(UUID id);

    GenreResponse updateGenre(UUID id, GenreRequest request);

    void deleteGenre(UUID id);
}
