package com.bookfair.notification_service.mapper;

import com.bookfair.notification_service.dto.NotificationDTO;
import com.bookfair.notification_service.dto.NotificationRequest;
import com.bookfair.notification_service.entity.Notification;
import com.bookfair.notification_service.entity.NotificationStatus;
import org.springframework.stereotype.Component;

/**
 * Mapper for Notification entity and DTOs
 */
@Component
public class NotificationMapper {

    public Notification toEntity(NotificationRequest request) {
        return Notification.builder()
                .userId(request.getUserId())
                .reservationId(request.getReservationId())
                .channel(request.getChannel())
                .status(NotificationStatus.PENDING)
                .templateCode(request.getTemplateCode())
                .subject(request.getSubject())
                .message(request.getMessage())
                .metadata(request.getMetadata())
                .scheduledFor(request.getScheduledFor())
                .maxRetries(request.getMaxRetries())
                .retryCount(0)
                .build();
    }

    public NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .reservationId(notification.getReservationId())
                .channel(notification.getChannel())
                .status(notification.getStatus())
                .templateCode(notification.getTemplateCode())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .metadata(notification.getMetadata())
                .retryCount(notification.getRetryCount())
                .maxRetries(notification.getMaxRetries())
                .scheduledFor(notification.getScheduledFor())
                .sentAt(notification.getSentAt())
                .errorReason(notification.getErrorReason())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
