package com.bookfair.reservation_service.repository;

import com.bookfair.reservation_service.entity.UserSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for UserSnapshot entity
 */
@Repository
public interface UserSnapshotRepository extends JpaRepository<UserSnapshot, UUID> {
}
