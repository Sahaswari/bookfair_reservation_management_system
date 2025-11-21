package com.bookfair.genre.repository;

import com.bookfair.genre.model.UserSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserSnapshotRepository extends JpaRepository<UserSnapshot, UUID> {
}

