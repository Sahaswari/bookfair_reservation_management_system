package com.bookfair.genre.service;

import com.bookfair.genre.dto.GenreRequest;
import com.bookfair.genre.dto.GenreResponse;
import com.bookfair.genre.model.Genre;
import com.bookfair.genre.model.UserSnapshot;
import com.bookfair.genre.repository.GenreRepository;
import com.bookfair.genre.repository.UserSnapshotRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepo;
    private final UserSnapshotRepository userRepo;

    public GenreServiceImpl(GenreRepository genreRepo, UserSnapshotRepository userRepo) {
        this.genreRepo = genreRepo;
        this.userRepo = userRepo;
    }

    @Override
    public GenreResponse createGenre(GenreRequest request) {
        Genre genre = new Genre();
        genre.setEventId(request.eventId());
        genre.setUserId(request.userId());
        genre.setGenreName(request.genreName());
        genre.setDescription(request.description());

        Genre saved = genreRepo.save(genre);

        return mapToResponse(saved);
    }

    @Override
    public List<GenreResponse> getAllGenres() {
        return genreRepo.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public GenreResponse getById(UUID id) {
        Genre genre = genreRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found"));

        return mapToResponse(genre);
    }

    @Override
    public GenreResponse updateGenre(UUID id, GenreRequest request) {
        Genre genre = genreRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found"));

        genre.setEventId(request.eventId());
        genre.setGenreName(request.genreName());
        genre.setDescription(request.description());

        Genre updated = genreRepo.save(genre);

        return mapToResponse(updated);
    }

    @Override
    public void deleteGenre(UUID id) {
        genreRepo.deleteById(id);
    }

    private GenreResponse mapToResponse(Genre genre) {
        UserSnapshot user = userRepo.findById(genre.getUserId()).orElse(null);

        return new GenreResponse(
                genre.getId(),
                genre.getEventId(),
                genre.getUserId(),
                genre.getGenreName(),
                genre.getDescription(),
                genre.getCreatedAt(),
                genre.getUpdatedAt(),
                user
        );
    }
}
