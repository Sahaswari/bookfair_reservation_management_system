package com.bookfair.genre_service.repository;

import com.bookfair.genre_service.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface GenreRepository extends JpaRepository<Genre, UUID> {
}
