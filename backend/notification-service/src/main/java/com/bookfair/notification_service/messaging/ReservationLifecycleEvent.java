package com.bookfair.notification_service.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka event published when a reservation is created, updated, or cancelled
 * This event is consumed by notification-service to update reservation_snapshot table
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationLifecycleEvent {

    private UUID eventId;
    private String eventType; // RESERVATION_CREATED, RESERVATION_CONFIRMED, RESERVATION_CANCELLED
    private Instant occurredAt;

    // Reservation data
    private UUID reservationId;
    private UUID userId;
    private UUID stallId;
    private UUID bookFairEventId;
    private LocalDate reservationDate;
    private String status; // PENDING, CONFIRMED, CANCELLED
    private String confirmationCode;
    private String qrCodeUrl;

    // User snapshot data for notification rendering
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private String userRole;
    private String userStatus;

    // Stall snapshot data for notification rendering
    private String stallCode;
    private String sizeCategory;
    private BigDecimal price;
    private Float locationX;
    private Float locationY;
}
