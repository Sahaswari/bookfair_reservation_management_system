package com.bookfair.genre.service;

import com.bookfair.genre.dto.GenreRequest;
import com.bookfair.genre.dto.GenreResponse;

import java.util.List;

public interface GenreService {

    List<GenreResponse> addGenres(String token, GenreRequest request);

    List<GenreResponse> getByEvent(Long eventId);

    List<GenreResponse> getByUser(Long userId);
}
