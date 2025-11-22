package com.bookfair.genre_service.service.impl;

import com.bookfair.genre_service.dto.GenreRequest;
import com.bookfair.genre_service.dto.GenreResponse;
import com.bookfair.genre_service.entity.Genre;
import com.bookfair.genre_service.entity.UserSnapshot;
import com.bookfair.genre_service.repository.GenreRepository;
import com.bookfair.genre_service.repository.UserSnapshotRepository;
import com.bookfair.genre_service.service.GenreService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final UserSnapshotRepository userSnapshotRepository;

    public GenreServiceImpl(GenreRepository genreRepository, UserSnapshotRepository userSnapshotRepository) {
        this.genreRepository = genreRepository;
        this.userSnapshotRepository = userSnapshotRepository;
    }

    @Override
    public GenreResponse createGenre(GenreRequest req) {
        UserSnapshot user = userSnapshotRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Genre g = new Genre();
        g.setId(UUID.randomUUID());
        g.setCode(req.getCode());
        g.setName(req.getName());
        g.setDescription(req.getDescription());
        g.setDisplayOrder(req.getDisplayOrder());
        g.setIsActive(req.getIsActive());
        g.setCreatedBy(user.getUserId());
        g.setUpdatedBy(user.getUserId());

        genreRepository.save(g);

        return toResponse(g);
    }

    @Override
    public List<GenreResponse> getAll() {
        return genreRepository.findAll()
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GenreResponse getById(UUID id) {
        Genre g = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found"));
        return toResponse(g);
    }

    @Override
    public GenreResponse updateGenre(UUID id, GenreRequest req) {
        Genre g = genreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Genre not found"));

        UserSnapshot user = userSnapshotRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        g.setCode(req.getCode());
        g.setName(req.getName());
        g.setDescription(req.getDescription());
        g.setDisplayOrder(req.getDisplayOrder());
        g.setIsActive(req.getIsActive());
        g.setUpdatedBy(user.getUserId());
        g.setUpdatedAt(java.time.LocalDateTime.now());

        genreRepository.save(g);

        return toResponse(g);
    }

    @Override
    public void deleteGenre(UUID id) {
        genreRepository.deleteById(id);
    }

    private GenreResponse toResponse(Genre g) {
        GenreResponse r = new GenreResponse();

        r.setId(g.getId());
        r.setCode(g.getCode());
        r.setName(g.getName());
        r.setDescription(g.getDescription());
        r.setDisplayOrder(g.getDisplayOrder());
        r.setIsActive(g.getIsActive());
        r.setCreatedAt(g.getCreatedAt());
        r.setUpdatedAt(g.getUpdatedAt());

        return r;
    }
}
