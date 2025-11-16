package com.bookfair.genre.repository;

import com.bookfair.genre.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    List<Genre> findByUserId(Long userId);

    List<Genre> findByEventId(Long eventId);

}
