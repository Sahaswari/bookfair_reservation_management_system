package com.bookfair.stall_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookfair.stall_service.entity.UserSnapshot;

/**
 * Repository for UserSnapshot entity
 */
@Repository
public interface UserSnapshotRepository extends JpaRepository<UserSnapshot, UUID> {
    // Basic CRUD operations inherited from JpaRepository
    
    /**
     * Find user snapshot by user ID
     * @param userId the user ID from Auth Service
     * @return Optional containing the UserSnapshot if found
     */
    java.util.Optional<UserSnapshot> findByUserId(UUID userId);
    
    /**
     * Check if user snapshot exists by user ID
     * @param userId the user ID from Auth Service
     * @return true if exists, false otherwise
     */
    boolean existsByUserId(UUID userId);
}
