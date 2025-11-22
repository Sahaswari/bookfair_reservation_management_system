package com.bookfair.notification_service.dto;

import com.bookfair.notification_service.entity.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for creating a notification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private UUID reservationId;

    @NotNull(message = "Channel is required")
    private NotificationChannel channel;

    private String templateCode;

    private String subject;

    @NotBlank(message = "Message is required")
    private String message;

    private Map<String, Object> metadata;

    private Instant scheduledFor;

    @Builder.Default
    private Integer maxRetries = 3;
}
