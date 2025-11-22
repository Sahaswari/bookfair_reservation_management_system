package com.bookfair.reservation_service.controller;

import com.bookfair.reservation_service.dto.ApiResponse;
import com.bookfair.reservation_service.dto.CreateReservationRequest;
import com.bookfair.reservation_service.dto.ReservationDTO;
import com.bookfair.reservation_service.dto.UpdateReservationStatusRequest;
import com.bookfair.reservation_service.entity.ReservationStatus;
import com.bookfair.reservation_service.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Reservation operations
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 1. Get all reservations
     * GET /api/reservations
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationDTO>>> getAllReservations() {
        try {
            log.info("REST request to get all reservations");
            List<ReservationDTO> reservations = reservationService.getAllReservations();
            return ResponseEntity.ok(ApiResponse.success("Reservations fetched successfully", reservations));
        } catch (Exception e) {
            log.error("Error fetching reservations", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 2. Create a new reservation (book a stall) with PENDING status
     * POST /api/reservations
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationDTO>> createReservation(
            @Valid @RequestBody CreateReservationRequest request) {
        try {
            log.info("REST request to create reservation for user: {} and stall: {}", 
                    request.getUserId(), request.getStallId());
            ReservationDTO reservation = reservationService.createReservation(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Reservation created successfully with PENDING status", reservation));
        } catch (Exception e) {
            log.error("Error creating reservation", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 3. Update reservation status (Admin privilege: PENDING -> CONFIRMED or CANCELLED)
     * PUT /api/reservations/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ReservationDTO>> updateReservationStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReservationStatusRequest request) {
        try {
            log.info("REST request to update reservation {} status to {}", id, request.getStatus());
            ReservationDTO reservation = reservationService.updateReservationStatus(id, request.getStatus());
            return ResponseEntity.ok(ApiResponse.success("Reservation status updated successfully", reservation));
        } catch (Exception e) {
            log.error("Error updating reservation status", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get reservation by ID
     * GET /api/reservations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationDTO>> getReservationById(@PathVariable UUID id) {
        try {
            log.info("REST request to get reservation with ID: {}", id);
            ReservationDTO reservation = reservationService.getReservationById(id);
            return ResponseEntity.ok(ApiResponse.success("Reservation fetched successfully", reservation));
        } catch (Exception e) {
            log.error("Error fetching reservation", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get reservations by status
     * GET /api/reservations/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ReservationDTO>>> getReservationsByStatus(
            @PathVariable ReservationStatus status) {
        try {
            log.info("REST request to get reservations with status: {}", status);
            List<ReservationDTO> reservations = reservationService.getReservationsByStatus(status);
            return ResponseEntity.ok(ApiResponse.success("Reservations fetched successfully", reservations));
        } catch (Exception e) {
            log.error("Error fetching reservations by status", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get reservations by user
     * GET /api/reservations/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ReservationDTO>>> getReservationsByUser(@PathVariable UUID userId) {
        try {
            log.info("REST request to get reservations for user: {}", userId);
            List<ReservationDTO> reservations = reservationService.getReservationsByUser(userId);
            return ResponseEntity.ok(ApiResponse.success("User reservations fetched successfully", reservations));
        } catch (Exception e) {
            log.error("Error fetching user reservations", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get reservations by event
     * GET /api/reservations/event/{eventId}
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<ApiResponse<List<ReservationDTO>>> getReservationsByEvent(@PathVariable UUID eventId) {
        try {
            log.info("REST request to get reservations for event: {}", eventId);
            List<ReservationDTO> reservations = reservationService.getReservationsByEvent(eventId);
            return ResponseEntity.ok(ApiResponse.success("Event reservations fetched successfully", reservations));
        } catch (Exception e) {
            log.error("Error fetching event reservations", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
