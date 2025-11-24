package com.bookfair.notification_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookfair.notification_service.entity.ReservationSnapshot;

/**
 * Repository for ReservationSnapshot entity
 */
@Repository
public interface ReservationSnapshotRepository extends JpaRepository<ReservationSnapshot, UUID> {
    
    /**
     * Find reservation snapshot by user ID
     */
    java.util.List<ReservationSnapshot> findByUserId(UUID userId);
    
    /**
     * Find reservation snapshot by event ID
     */
    java.util.List<ReservationSnapshot> findByEventId(UUID eventId);
    
    /**
     * Find reservation snapshot by status
     */
    java.util.List<ReservationSnapshot> findByStatus(String status);

    /**
     * Find a snapshot by the reservation identifier carried in Kafka events
     */
    java.util.Optional<ReservationSnapshot> findByReservationId(UUID reservationId);
}
