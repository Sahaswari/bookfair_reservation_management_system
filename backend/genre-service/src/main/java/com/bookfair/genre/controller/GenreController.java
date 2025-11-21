package com.bookfair.genre.controller;

import com.bookfair.genre.dto.GenreRequest;
import com.bookfair.genre.dto.GenreResponse;
import com.bookfair.genre.service.GenreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/genres")
public class GenreController {

    private final GenreService service;

    public GenreController(GenreService service) {
        this.service = service;
    }

    @PostMapping
    public GenreResponse create(@RequestBody GenreRequest request) {
        return service.createGenre(request);
    }

    @GetMapping
    public List<GenreResponse> getAll() {
        return service.getAllGenres();
    }

    @GetMapping("/{id}")
    public GenreResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public GenreResponse update(
            @PathVariable UUID id,
            @RequestBody GenreRequest request) {
        return service.updateGenre(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.deleteGenre(id);
    }
}

