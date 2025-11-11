package com.bookfair.stall_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bookfair.stall_service.entity.Stall;
import com.bookfair.stall_service.entity.StallSize;

/**
 * Repository for Stall entity
 */
@Repository
public interface StallRepository extends JpaRepository<Stall, UUID> {
    
    // Find stall by stall code
    Optional<Stall> findByStallCode(String stallCode);
    
    // Find all stalls for a specific event
    List<Stall> findByEventId(UUID eventId);
    
    // Find available stalls for an event
    @Query("SELECT s FROM Stall s WHERE s.event.id = :eventId AND s.isReserved = false")
    List<Stall> findAvailableStallsByEventId(@Param("eventId") UUID eventId);
    
    // Find reserved stalls for an event
    @Query("SELECT s FROM Stall s WHERE s.event.id = :eventId AND s.isReserved = true")
    List<Stall> findReservedStallsByEventId(@Param("eventId") UUID eventId);
    
    // Find stalls by size category for an event
    List<Stall> findByEventIdAndSizeCategory(UUID eventId, StallSize sizeCategory);
    
    // Find stalls reserved by a specific vendor
    List<Stall> findByReservedBy(UUID vendorId);
    
    // Find available stalls by size for an event
    @Query("SELECT s FROM Stall s WHERE s.event.id = :eventId AND s.sizeCategory = :size AND s.isReserved = false")
    List<Stall> findAvailableStallsByEventIdAndSize(@Param("eventId") UUID eventId, @Param("size") StallSize size);
    
    // Count available stalls for an event
    @Query("SELECT COUNT(s) FROM Stall s WHERE s.event.id = :eventId AND s.isReserved = false")
    Long countAvailableStallsByEventId(@Param("eventId") UUID eventId);
    
    // Check if stall code exists
    boolean existsByStallCode(String stallCode);
}
