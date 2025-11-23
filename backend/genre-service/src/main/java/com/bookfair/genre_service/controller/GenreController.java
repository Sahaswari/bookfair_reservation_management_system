package com.bookfair.genre_service.controller;

import com.bookfair.genre_service.dto.GenreRequest;
import com.bookfair.genre_service.dto.GenreResponse;
import com.bookfair.genre_service.service.GenreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/genres")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @PostMapping
    public GenreResponse create(@RequestBody GenreRequest req) {
        return genreService.createGenre(req);
    }

    @GetMapping
    public List<GenreResponse> getAll() {
        return genreService.getAll();
    }

    @GetMapping("/{id}")
    public GenreResponse getById(@PathVariable UUID id) {
        return genreService.getById(id);
    }

    @PutMapping("/{id}")
    public GenreResponse update(@PathVariable UUID id, @RequestBody GenreRequest req) {
        return genreService.updateGenre(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        genreService.deleteGenre(id);
    }

    @GetMapping("/user/{userId}")
    public List<GenreResponse> getByUser(@PathVariable UUID userId) {
        return genreService.getGenresByUser(userId);
    }
}
