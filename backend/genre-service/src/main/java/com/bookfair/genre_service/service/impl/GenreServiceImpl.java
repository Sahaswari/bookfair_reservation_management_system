package com.bookfair.genre_service.service.impl;

import com.bookfair.genre_service.dto.GenreRequest;
import com.bookfair.genre_service.dto.GenreResponse;
import com.bookfair.genre_service.entity.Genre;
import com.bookfair.genre_service.entity.UserSnapshot;
import com.bookfair.genre_service.exception.BadRequestException;
import com.bookfair.genre_service.exception.DuplicateResourceException;
import com.bookfair.genre_service.exception.ResourceNotFoundException;
import com.bookfair.genre_service.repository.GenreRepository;
import com.bookfair.genre_service.repository.UserSnapshotRepository;
import com.bookfair.genre_service.service.GenreService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

        // Validate request
        if (req.getName() == null || req.getName().isBlank()) {
            throw new BadRequestException("Genre name is required.");
        }
        if (req.getCode() == null || req.getCode().isBlank()) {
            throw new BadRequestException("Genre code is required.");
        }

        // Check duplicates
        if (genreRepository.existsByName(req.getName())) {
            throw new DuplicateResourceException("Genre name already exists.");
        }
        if (genreRepository.existsByCode(req.getCode())) {
            throw new DuplicateResourceException("Genre code already exists.");
        }

        UserSnapshot user = userSnapshotRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + req.getUserId()));

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
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GenreResponse getById(UUID id) {
        Genre g = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));
        return toResponse(g);
    }

    @Override
    public GenreResponse updateGenre(UUID id, GenreRequest req) {

        Genre g = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));

        UserSnapshot user = userSnapshotRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + req.getUserId()));

        // Duplicate checks (except same record)
        if (!g.getName().equals(req.getName()) && genreRepository.existsByName(req.getName())) {
            throw new DuplicateResourceException("Genre name already exists.");
        }

        if (!g.getCode().equals(req.getCode()) && genreRepository.existsByCode(req.getCode())) {
            throw new DuplicateResourceException("Genre code already exists.");
        }

        g.setCode(req.getCode());
        g.setName(req.getName());
        g.setDescription(req.getDescription());
        g.setDisplayOrder(req.getDisplayOrder());
        g.setIsActive(req.getIsActive());
        g.setUpdatedBy(user.getUserId());
        g.setUpdatedAt(LocalDateTime.now());

        genreRepository.save(g);

        return toResponse(g);
    }

    @Override
    public void deleteGenre(UUID id) {
        Genre g = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));
        genreRepository.delete(g);
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
