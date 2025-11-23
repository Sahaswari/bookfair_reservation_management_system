package com.bookfair.genre_service.repository;

import com.bookfair.genre_service.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GenreRepository extends JpaRepository<Genre, UUID> {

    boolean existsByName(String name);

    boolean existsByCode(String code);
}
