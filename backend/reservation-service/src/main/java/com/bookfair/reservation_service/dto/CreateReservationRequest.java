package com.bookfair.reservation_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a new reservation. Only identifiers are collected from the caller;
 * related data is resolved server-side from snapshot tables.
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
}
