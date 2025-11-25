package com.bookfair.genre_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bookfair.genre_service.entity.UserSnapshot;

public interface UserSnapshotRepository extends JpaRepository<UserSnapshot, UUID> {
}
