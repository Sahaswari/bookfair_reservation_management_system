package com.bookfair.reservation_service.repository;

import com.bookfair.reservation_service.entity.StallSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for StallSnapshot entity
 */
@Repository
public interface StallSnapshotRepository extends JpaRepository<StallSnapshot, UUID> {
}
