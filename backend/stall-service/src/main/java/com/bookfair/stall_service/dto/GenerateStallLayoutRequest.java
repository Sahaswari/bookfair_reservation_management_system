package com.bookfair.stall_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request payload for generating a batch of stalls based on a predefined layout.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateStallLayoutRequest {

    /**
     * Target event that should receive the generated stalls.
     */
    @NotNull(message = "eventId is required")
    private UUID eventId;
}
