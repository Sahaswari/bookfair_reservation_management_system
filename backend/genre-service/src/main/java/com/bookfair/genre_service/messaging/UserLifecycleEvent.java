package com.bookfair.genre_service.messaging;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String mobileNo;
    private String role;
    private String status;
}
