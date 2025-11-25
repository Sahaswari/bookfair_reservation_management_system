package com.bookfair.reservation_service.dto;

import com.bookfair.reservation_service.entity.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating reservation status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReservationStatusRequest {

    @NotNull(message = "Status is required")
    private ReservationStatus status;
}
