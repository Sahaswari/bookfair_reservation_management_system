package com.bookfair.genre.service.impl;

import com.bookfair.genre.client.AuthClient;
import com.bookfair.genre.client.EventClient;
import com.bookfair.genre.dto.GenreRequest;
import com.bookfair.genre.dto.GenreResponse;
import com.bookfair.genre.dto.UserSnapshot;
import com.bookfair.genre.entity.Genre;
import com.bookfair.genre.repository.GenreRepository;
import com.bookfair.genre.service.GenreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final AuthClient authClient;
    private final EventClient eventClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<GenreResponse> addGenres(String token, GenreRequest request) {

        UserSnapshot snapshot = authClient.getUserInfo(token);

        boolean valid = eventClient.validateEvent(request.eventId());
        if (!valid) throw new RuntimeException("Invalid event ID");

        return request.genres().stream()
                .map(g -> saveGenre(request.eventId(), g, snapshot))
                .toList();
    }

    private GenreResponse saveGenre(Long eventId, String genre, UserSnapshot snapshot) {

        try {
            String json = mapper.writeValueAsString(snapshot);

            Genre saved = genreRepository.save(
                    Genre.builder()
                            .eventId(eventId)
                            .genreName(genre)
                            .userId(snapshot.userId())
                            .userSnapshot(json)
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            return new GenreResponse(
                    saved.getId(),
                    saved.getEventId(),
                    saved.getGenreName(),
                    saved.getUserSnapshot()
            );

        } catch (Exception e) {
            throw new RuntimeException("Error saving genre");
        }
    }

    @Override
    public List<GenreResponse> getByEvent(Long eventId) {
        return genreRepository.findByEventId(eventId)
                .stream()
                .map(g -> new GenreResponse(g.getId(), g.getEventId(), g.getGenreName(), g.getUserSnapshot()))
                .toList();
    }

    @Override
    public List<GenreResponse> getByUser(Long userId) {
        return genreRepository.findByUserId(userId)
                .stream()
                .map(g -> new GenreResponse(g.getId(), g.getEventId(), g.getGenreName(), g.getUserSnapshot()))
                .toList();
    }
}
