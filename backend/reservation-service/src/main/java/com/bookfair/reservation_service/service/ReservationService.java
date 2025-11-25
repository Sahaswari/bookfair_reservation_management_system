package com.bookfair.reservation_service.service;

import com.bookfair.reservation_service.dto.CreateReservationRequest;
import com.bookfair.reservation_service.dto.ReservationDTO;
import com.bookfair.reservation_service.entity.Reservation;
import com.bookfair.reservation_service.entity.ReservationStatus;
import com.bookfair.reservation_service.entity.StallSnapshot;
import com.bookfair.reservation_service.entity.UserSnapshot;
import com.bookfair.reservation_service.exception.InvalidOperationException;
import com.bookfair.reservation_service.exception.ResourceNotFoundException;
import com.bookfair.reservation_service.messaging.ReservationEventProducer;
import com.bookfair.reservation_service.messaging.ReservationLifecycleEvent;
import com.bookfair.reservation_service.repository.ReservationRepository;
import com.bookfair.reservation_service.repository.StallSnapshotRepository;
import com.bookfair.reservation_service.repository.UserSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for Reservation operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserSnapshotRepository userSnapshotRepository;
    private final StallSnapshotRepository stallSnapshotRepository;
    private final ReservationEventProducer reservationEventProducer;

    /**
     * Get all reservations
     */
    @Transactional(readOnly = true)
    public List<ReservationDTO> getAllReservations() {
        log.info("Fetching all reservations");
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get reservation by ID
     */
    @Transactional(readOnly = true)
    public ReservationDTO getReservationById(UUID id) {
        log.info("Fetching reservation with ID: {}", id);
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + id));
        return convertToDTO(reservation);
    }

    /**
     * Create a new reservation with PENDING status
     */
    @Transactional
    public ReservationDTO createReservation(CreateReservationRequest request) {
        log.info("Creating new reservation for user: {} and stall: {}", request.getUserId(), request.getStallId());

        // Check if stall already has an active reservation
        boolean hasActiveReservation = reservationRepository.existsByStallIdAndStatus(
                request.getStallId(), ReservationStatus.PENDING) ||
                reservationRepository.existsByStallIdAndStatus(
                        request.getStallId(), ReservationStatus.CONFIRMED);

        if (hasActiveReservation) {
            throw new InvalidOperationException("Stall is already reserved");
        }

        // Create or update user snapshot if data provided
        if (request.getUserFirstName() != null) {
            UserSnapshot userSnapshot = new UserSnapshot();
            userSnapshot.setUserId(request.getUserId());
            userSnapshot.setFirstName(request.getUserFirstName());
            userSnapshot.setLastName(request.getUserLastName());
            userSnapshot.setEmail(request.getUserEmail());
            userSnapshot.setRole(request.getUserRole());
            userSnapshot.setStatus(request.getUserStatus());
            userSnapshotRepository.save(userSnapshot);
        }

        // Create or update stall snapshot if data provided
        if (request.getStallCode() != null) {
            StallSnapshot stallSnapshot = new StallSnapshot();
            stallSnapshot.setStallId(request.getStallId());
            stallSnapshot.setEventId(request.getEventId());
            stallSnapshot.setStallCode(request.getStallCode());
            stallSnapshot.setSizeCategory(request.getSizeCategory());
            if (request.getPrice() != null) {
                stallSnapshot.setPrice(new BigDecimal(request.getPrice()));
            }
            stallSnapshot.setLocationX(request.getLocationX());
            stallSnapshot.setLocationY(request.getLocationY());
            stallSnapshotRepository.save(stallSnapshot);
        }

        // Create reservation with PENDING status
        Reservation reservation = new Reservation();
        reservation.setUserId(request.getUserId());
        reservation.setStallId(request.getStallId());
        reservation.setEventId(request.getEventId());
        reservation.setReservationDate(request.getReservationDate());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setConfirmationCode(generateConfirmationCode());

        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Reservation created successfully with ID: {}", savedReservation.getId());

        // Publish reservation creation event to Kafka
        publishReservationCreatedEvent(savedReservation, request);

        return convertToDTO(savedReservation);
    }

    /**
     * Update reservation status (admin privilege)
     */
    @Transactional
    public ReservationDTO updateReservationStatus(UUID id, ReservationStatus newStatus) {
        log.info("Updating reservation {} status to {}", id, newStatus);

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + id));

        // Validate status transition
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot update status of a cancelled reservation");
        }

        ReservationStatus oldStatus = reservation.getStatus();
        reservation.setStatus(newStatus);

        if (newStatus == ReservationStatus.CONFIRMED) {
            // Generate QR code URL when confirmed
            reservation.setQrCodeUrl(generateQRCodeUrl(reservation.getId()));
        }

        Reservation updatedReservation = reservationRepository.save(reservation);
        log.info("Reservation status updated successfully");

        // Publish status change event to Kafka
        publishReservationStatusChangedEvent(updatedReservation, oldStatus, newStatus);

        return convertToDTO(updatedReservation);
    }

    /**
     * Get reservations by status
     */
    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByStatus(ReservationStatus status) {
        log.info("Fetching reservations with status: {}", status);
        List<Reservation> reservations = reservationRepository.findByStatus(status);
        return reservations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get reservations by user
     */
    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByUser(UUID userId) {
        log.info("Fetching reservations for user: {}", userId);
        List<Reservation> reservations = reservationRepository.findByUserId(userId);
        return reservations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get reservations by event
     */
    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByEvent(UUID eventId) {
        log.info("Fetching reservations for event: {}", eventId);
        List<Reservation> reservations = reservationRepository.findByEventId(eventId);
        return reservations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Delete reservation (admin privilege)
     */
    @Transactional
    public void deleteReservation(UUID id) {
        log.info("Deleting reservation with ID: {}", id);
        
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with ID: " + id));
        
        publishReservationDeletedEvent(reservation);
        reservationRepository.delete(reservation);
        log.info("Reservation deleted successfully with ID: {}", id);
    }

    /**
     * Convert Reservation entity to DTO
     */
    private ReservationDTO convertToDTO(Reservation reservation) {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(reservation.getId());
        dto.setUserId(reservation.getUserId());
        dto.setStallId(reservation.getStallId());
        dto.setEventId(reservation.getEventId());
        dto.setReservationDate(reservation.getReservationDate());
        dto.setStatus(reservation.getStatus());
        dto.setConfirmationCode(reservation.getConfirmationCode());
        dto.setQrCodeUrl(reservation.getQrCodeUrl());
        dto.setCreatedAt(reservation.getCreatedAt());
        dto.setUpdatedAt(reservation.getUpdatedAt());

        // Add user snapshot data if available
        userSnapshotRepository.findById(reservation.getUserId()).ifPresent(userSnapshot -> {
            dto.setUserFirstName(userSnapshot.getFirstName());
            dto.setUserLastName(userSnapshot.getLastName());
            dto.setUserEmail(userSnapshot.getEmail());
        });

        // Add stall snapshot data if available
        stallSnapshotRepository.findById(reservation.getStallId()).ifPresent(stallSnapshot -> {
            dto.setStallCode(stallSnapshot.getStallCode());
            dto.setSizeCategory(stallSnapshot.getSizeCategory());
            dto.setPrice(stallSnapshot.getPrice());
            dto.setLocationX(stallSnapshot.getLocationX());
            dto.setLocationY(stallSnapshot.getLocationY());
        });

        return dto;
    }

    /**
     * Generate a unique confirmation code
     */
    private String generateConfirmationCode() {
        return "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Generate QR code URL
     */
    private String generateQRCodeUrl(UUID reservationId) {
        return "/api/reservations/" + reservationId + "/qrcode";
    }

    /**
     * Publish reservation created event to Kafka
     */
    private void publishReservationCreatedEvent(Reservation reservation, CreateReservationRequest request) {
        try {
            // Fetch user snapshot data
            String userFirstName = request.getUserFirstName();
            String userLastName = request.getUserLastName();
            String userEmail = request.getUserEmail();
            String userRole = request.getUserRole();
            String userStatus = request.getUserStatus();

            // Fetch stall snapshot data
            String stallCode = request.getStallCode();
            String sizeCategory = request.getSizeCategory();
            BigDecimal price = request.getPrice() != null ? new BigDecimal(request.getPrice()) : null;
            Float locationX = request.getLocationX() != null ? request.getLocationX().floatValue() : null;
            Float locationY = request.getLocationY() != null ? request.getLocationY().floatValue() : null;

            // Build and publish event
            ReservationLifecycleEvent event = ReservationLifecycleEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventType("RESERVATION_CREATED")
                    .occurredAt(Instant.now())
                    .reservationId(reservation.getId())
                    .userId(reservation.getUserId())
                    .stallId(reservation.getStallId())
                    .bookFairEventId(reservation.getEventId())
                    .reservationDate(reservation.getReservationDate())
                    .status(reservation.getStatus().toString())
                    .confirmationCode(reservation.getConfirmationCode())
                    .qrCodeUrl(reservation.getQrCodeUrl())
                    .userFirstName(userFirstName)
                    .userLastName(userLastName)
                    .userEmail(userEmail)
                    .userRole(userRole)
                    .userStatus(userStatus)
                    .stallCode(stallCode)
                    .sizeCategory(sizeCategory)
                    .price(price)
                    .locationX(locationX)
                    .locationY(locationY)
                    .build();

            reservationEventProducer.publishReservationEvent(event);
            log.info("Published RESERVATION_CREATED event for reservation {}", reservation.getId());

        } catch (Exception e) {
            log.error("Error publishing RESERVATION_CREATED event for reservation {}", reservation.getId(), e);
            // Don't throw - allow the reservation to be created even if event publishing fails
        }
    }

    /**
     * Publish reservation status changed event to Kafka
     */
    private void publishReservationStatusChangedEvent(Reservation reservation, 
                                                     ReservationStatus oldStatus, 
                                                     ReservationStatus newStatus) {
        try {
            // Fetch user snapshot data
            String userFirstName = null;
            String userLastName = null;
            String userEmail = null;
            String userRole = null;
            String userStatus = null;

            UserSnapshot userSnapshot = userSnapshotRepository.findById(reservation.getUserId()).orElse(null);
            if (userSnapshot != null) {
                userFirstName = userSnapshot.getFirstName();
                userLastName = userSnapshot.getLastName();
                userEmail = userSnapshot.getEmail();
                userRole = userSnapshot.getRole();
                userStatus = userSnapshot.getStatus();
            }

            // Fetch stall snapshot data
            String stallCode = null;
            String sizeCategory = null;
            BigDecimal price = null;
            Float locationX = null;
            Float locationY = null;

            StallSnapshot stallSnapshot = stallSnapshotRepository.findById(reservation.getStallId()).orElse(null);
            if (stallSnapshot != null) {
                stallCode = stallSnapshot.getStallCode();
                sizeCategory = stallSnapshot.getSizeCategory();
                price = stallSnapshot.getPrice();
                if (stallSnapshot.getLocationX() != null) {
                    locationX = stallSnapshot.getLocationX().floatValue();
                }
                if (stallSnapshot.getLocationY() != null) {
                    locationY = stallSnapshot.getLocationY().floatValue();
                }
            }

            // Determine event type based on status change
            String eventType = newStatus == ReservationStatus.CONFIRMED ? 
                    "RESERVATION_CONFIRMED" : "RESERVATION_CANCELLED";

            // Build and publish event
            ReservationLifecycleEvent event = ReservationLifecycleEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventType(eventType)
                    .occurredAt(Instant.now())
                    .reservationId(reservation.getId())
                    .userId(reservation.getUserId())
                    .stallId(reservation.getStallId())
                    .bookFairEventId(reservation.getEventId())
                    .reservationDate(reservation.getReservationDate())
                    .status(reservation.getStatus().toString())
                    .confirmationCode(reservation.getConfirmationCode())
                    .qrCodeUrl(reservation.getQrCodeUrl())
                    .userFirstName(userFirstName)
                    .userLastName(userLastName)
                    .userEmail(userEmail)
                    .userRole(userRole)
                    .userStatus(userStatus)
                    .stallCode(stallCode)
                    .sizeCategory(sizeCategory)
                    .price(price)
                    .locationX(locationX)
                    .locationY(locationY)
                    .build();

            reservationEventProducer.publishReservationEvent(event);
            log.info("Published {} event for reservation {}", eventType, reservation.getId());

        } catch (Exception e) {
            log.error("Error publishing status change event for reservation {}", reservation.getId(), e);
            // Don't throw - allow the status update to succeed even if event publishing fails
        }
    }

    /**
     * Publish reservation deleted event to Kafka
     */
    private void publishReservationDeletedEvent(Reservation reservation) {
        try {
            // Fetch user snapshot data
            String userFirstName = null;
            String userLastName = null;
            String userEmail = null;
            String userRole = null;
            String userStatus = null;

            UserSnapshot userSnapshot = userSnapshotRepository.findById(reservation.getUserId()).orElse(null);
            if (userSnapshot != null) {
                userFirstName = userSnapshot.getFirstName();
                userLastName = userSnapshot.getLastName();
                userEmail = userSnapshot.getEmail();
                userRole = userSnapshot.getRole();
                userStatus = userSnapshot.getStatus();
            }

            // Fetch stall snapshot data
            String stallCode = null;
            String sizeCategory = null;
            BigDecimal price = null;
            Float locationX = null;
            Float locationY = null;

            StallSnapshot stallSnapshot = stallSnapshotRepository.findById(reservation.getStallId()).orElse(null);
            if (stallSnapshot != null) {
                stallCode = stallSnapshot.getStallCode();
                sizeCategory = stallSnapshot.getSizeCategory();
                price = stallSnapshot.getPrice();
                if (stallSnapshot.getLocationX() != null) {
                    locationX = stallSnapshot.getLocationX().floatValue();
                }
                if (stallSnapshot.getLocationY() != null) {
                    locationY = stallSnapshot.getLocationY().floatValue();
                }
            }

            // Build and publish event
            ReservationLifecycleEvent event = ReservationLifecycleEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventType("RESERVATION_DELETED")
                    .occurredAt(Instant.now())
                    .reservationId(reservation.getId())
                    .userId(reservation.getUserId())
                    .stallId(reservation.getStallId())
                    .bookFairEventId(reservation.getEventId())
                    .reservationDate(reservation.getReservationDate())
                    .status(reservation.getStatus().toString())
                    .confirmationCode(reservation.getConfirmationCode())
                    .qrCodeUrl(reservation.getQrCodeUrl())
                    .userFirstName(userFirstName)
                    .userLastName(userLastName)
                    .userEmail(userEmail)
                    .userRole(userRole)
                    .userStatus(userStatus)
                    .stallCode(stallCode)
                    .sizeCategory(sizeCategory)
                    .price(price)
                    .locationX(locationX)
                    .locationY(locationY)
                    .build();

            reservationEventProducer.publishReservationEvent(event);
            log.info("Published RESERVATION_DELETED event for reservation {}", reservation.getId());

        } catch (Exception e) {
            log.error("Error publishing deletion event for reservation {}", reservation.getId(), e);
            // Don't throw - allow the deletion to succeed even if event publishing fails
        }
    }
}

