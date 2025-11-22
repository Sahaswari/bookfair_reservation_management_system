package com.bookfair.genre_service.repository;

import com.bookfair.genre_service.entity.UserSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserSnapshotRepository extends JpaRepository<UserSnapshot, UUID> {
}
