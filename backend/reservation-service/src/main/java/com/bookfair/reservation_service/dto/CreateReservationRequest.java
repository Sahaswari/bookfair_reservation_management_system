package com.bookfair.reservation_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating a new reservation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Stall ID is required")
    private UUID stallId;

    @NotNull(message = "Event ID is required")
    private UUID eventId;

    @NotNull(message = "Reservation date is required")
    private LocalDate reservationDate;

    // Optional user snapshot data
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private String userRole;
    private String userStatus;

    // Optional stall snapshot data
    private String stallCode;
    private String sizeCategory;
    private String price;
    private Float locationX;
    private Float locationY;
}
