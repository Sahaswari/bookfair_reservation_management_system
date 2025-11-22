package com.bookfair.reservation_service.dto;

import com.bookfair.reservation_service.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reservation DTO for API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    private UUID id;
    private UUID userId;
    private UUID stallId;
    private UUID eventId;
    private LocalDate reservationDate;
    private ReservationStatus status;
    private String confirmationCode;
    private String qrCodeUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // User information from snapshot
    private String userFirstName;
    private String userLastName;
    private String userEmail;

    // Stall information from snapshot
    private String stallCode;
    private String sizeCategory;
    private BigDecimal price;
    private Float locationX;
    private Float locationY;
}
