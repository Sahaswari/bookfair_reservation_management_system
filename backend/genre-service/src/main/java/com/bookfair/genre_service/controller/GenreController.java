package com.bookfair.genre.controller;

import com.bookfair.genre.dto.GenreRequest;
import com.bookfair.genre.dto.GenreResponse;
import com.bookfair.genre.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @PostMapping
    public List<GenreResponse> addGenres(
            @RequestHeader("Authorization") String token,
            @RequestBody GenreRequest request
    ) {
        return genreService.addGenres(token, request);
    }

    @GetMapping("/event/{eventId}")
    public List<GenreResponse> getByEvent(@PathVariable Long eventId) {
        return genreService.getByEvent(eventId);
    }

    @GetMapping("/user/{userId}")
    public List<GenreResponse> getByUser(@PathVariable Long userId) {
        return genreService.getByUser(userId);
    }
}
