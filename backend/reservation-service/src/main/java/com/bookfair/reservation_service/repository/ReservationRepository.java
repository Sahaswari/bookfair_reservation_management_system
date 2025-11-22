package com.bookfair.reservation_service.repository;

import com.bookfair.reservation_service.entity.Reservation;
import com.bookfair.reservation_service.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Reservation entity
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    /**
     * Find reservations by user ID
     */
    List<Reservation> findByUserId(UUID userId);

    /**
     * Find reservations by stall ID
     */
    List<Reservation> findByStallId(UUID stallId);

    /**
     * Find reservations by event ID
     */
    List<Reservation> findByEventId(UUID eventId);

    /**
     * Find reservations by status
     */
    List<Reservation> findByStatus(ReservationStatus status);

    /**
     * Find reservations by user ID and status
     */
    List<Reservation> findByUserIdAndStatus(UUID userId, ReservationStatus status);

    /**
     * Check if a reservation exists for a specific stall and status
     */
    boolean existsByStallIdAndStatus(UUID stallId, ReservationStatus status);
}
