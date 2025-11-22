package com.bookfair.reservation_service.service;

import com.bookfair.reservation_service.dto.CreateReservationRequest;
import com.bookfair.reservation_service.dto.ReservationDTO;
import com.bookfair.reservation_service.entity.Reservation;
import com.bookfair.reservation_service.entity.ReservationStatus;
import com.bookfair.reservation_service.entity.StallSnapshot;
import com.bookfair.reservation_service.entity.UserSnapshot;
import com.bookfair.reservation_service.exception.InvalidOperationException;
import com.bookfair.reservation_service.exception.ResourceNotFoundException;
import com.bookfair.reservation_service.repository.ReservationRepository;
import com.bookfair.reservation_service.repository.StallSnapshotRepository;
import com.bookfair.reservation_service.repository.UserSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

        reservation.setStatus(newStatus);

        if (newStatus == ReservationStatus.CONFIRMED) {
            // Generate QR code URL when confirmed
            reservation.setQrCodeUrl(generateQRCodeUrl(reservation.getId()));
        }

        Reservation updatedReservation = reservationRepository.save(reservation);
        log.info("Reservation status updated successfully");

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
}
