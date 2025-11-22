package com.bookfair.auth_service.messaging;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Immutable payload that describes lifecycle changes of a user captured by the Auth Service.
 * The event is serialized as JSON and propagated to downstream services via Kafka.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLifecycleEvent {

    private UUID eventId;
    private String eventType;
    private Instant occurredAt;

    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String companyName;
    private String role;
    private String status;
}
