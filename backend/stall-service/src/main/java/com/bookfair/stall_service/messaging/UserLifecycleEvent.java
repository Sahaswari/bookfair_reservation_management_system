package com.bookfair.stall_service.messaging;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Canonical representation of user lifecycle messages produced by the Auth Service.
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
