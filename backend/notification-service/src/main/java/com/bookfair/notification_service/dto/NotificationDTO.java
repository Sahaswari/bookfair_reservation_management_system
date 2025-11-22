package com.bookfair.notification_service.dto;

import com.bookfair.notification_service.entity.NotificationChannel;
import com.bookfair.notification_service.entity.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for Notification response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private UUID id;
    private UUID userId;
    private UUID reservationId;
    private NotificationChannel channel;
    private NotificationStatus status;
    private String templateCode;
    private String subject;
    private String message;
    private Map<String, Object> metadata;
    private Integer retryCount;
    private Integer maxRetries;
    private Instant scheduledFor;
    private Instant sentAt;
    private String errorReason;
    private Instant createdAt;
    private Instant updatedAt;
}
