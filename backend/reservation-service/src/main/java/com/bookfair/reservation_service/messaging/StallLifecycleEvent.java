package com.bookfair.reservation_service.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a lifecycle event for a Stall entity.
 * This event is consumed from Kafka whenever a stall is created, updated, or deleted.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StallLifecycleEvent {

    private UUID eventId;
    private String eventType; // e.g., STALL_CREATED, STALL_UPDATED, STALL_DELETED
    private Instant occurredAt;

    private UUID stallId;
    private UUID bookFairEventId; // Maps to 'event' entity in Stall Service
    private String stallCode;
    private String sizeCategory;
    private BigDecimal price;
    private Float locationX;
    private Float locationY;
}
