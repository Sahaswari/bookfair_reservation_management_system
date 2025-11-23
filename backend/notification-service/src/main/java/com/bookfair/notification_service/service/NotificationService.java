package com.bookfair.notification_service.service;

import com.bookfair.notification_service.dto.NotificationDTO;
import com.bookfair.notification_service.dto.NotificationRequest;
import com.bookfair.notification_service.entity.*;
import com.bookfair.notification_service.exception.NotificationException;
import com.bookfair.notification_service.mapper.NotificationMapper;
import com.bookfair.notification_service.repository.NotificationRepository;
import com.bookfair.notification_service.repository.UserSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserSnapshotRepository userSnapshotRepository;
    private final EmailService emailService;
    private final TemplateService templateService;
    private final NotificationMapper notificationMapper;

    /**
     * Create and send a notification
     *
     * @param request Notification request
     * @return Created notification
     */
    @Transactional
    public NotificationDTO createNotification(NotificationRequest request) {
        log.info("Creating notification for user: {} via {}", request.getUserId(), request.getChannel());

        // Validate user exists
        UserSnapshot user = userSnapshotRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotificationException("User not found: " + request.getUserId()));

        // Create notification entity
        Notification notification = notificationMapper.toEntity(request);
        notification = notificationRepository.save(notification);

        // Send immediately if not scheduled
        if (request.getScheduledFor() == null || request.getScheduledFor().isBefore(Instant.now())) {
            sendNotification(notification, user);
        }

        return notificationMapper.toDTO(notification);
    }

    /**
     * Send a notification
     *
     * @param notification Notification to send
     * @param user         User snapshot
     */
    private void sendNotification(Notification notification, UserSnapshot user) {
        try {
            log.info("Sending {} notification to user: {}", notification.getChannel(), user.getUserId());

            switch (notification.getChannel()) {
                case EMAIL -> sendEmailNotification(notification, user);
                case SMS -> sendSmsNotification(notification, user);
                case IN_APP -> sendInAppNotification(notification, user);
                default -> throw new NotificationException("Unsupported notification channel: " + notification.getChannel());
            }

            notification.markAsSent();
            notificationRepository.save(notification);
            log.info("Notification sent successfully: {}", notification.getId());

        } catch (Exception e) {
            log.error("Failed to send notification: {}", notification.getId(), e);
            notification.markAsFailed(e.getMessage());
            notificationRepository.save(notification);
            throw new NotificationException("Failed to send notification", e);
        }
    }

    /**
     * Send email notification
     */
    private void sendEmailNotification(Notification notification, UserSnapshot user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new NotificationException("User email is not available");
        }

        String subject = notification.getSubject() != null ? notification.getSubject() : "Book Fair Notification";
        emailService.sendEmail(user.getEmail(), subject, notification.getMessage());
    }

    /**
     * Send SMS notification
     */
    private void sendSmsNotification(Notification notification, UserSnapshot user) {
        if (user.getPhone() == null || user.getPhone().isBlank()) {
            throw new NotificationException("User phone number is not available");
        }

        // TODO: Implement SMS service integration (Twilio, AWS SNS, etc.)
        log.info("SMS notification would be sent to: {} - Message: {}", user.getPhone(), notification.getMessage());
    }

    /**
     * Send in-app notification
     */
    private void sendInAppNotification(Notification notification, UserSnapshot user) {
        // In-app notifications are just stored in DB and retrieved by frontend
        log.info("In-app notification created for user: {}", user.getUserId());
    }

    /**
     * Get notification by ID
     */
    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationException("Notification not found: " + id));
        return notificationMapper.toDTO(notification);
    }

    /**
     * Get all notifications for a user
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsByUserId(UUID userId) {
        return notificationRepository.findByUserId(userId).stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all notifications for a reservation
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsByReservationId(UUID reservationId) {
        return notificationRepository.findByReservationId(reservationId).stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all notifications by status
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsByStatus(NotificationStatus status) {
        return notificationRepository.findByStatus(status).stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retry failed notification
     */
    @Transactional
    public NotificationDTO retryNotification(UUID notificationId) {
        log.info("Retrying notification: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException("Notification not found: " + notificationId));

        if (!notification.canRetry()) {
            throw new NotificationException("Notification has reached maximum retry attempts");
        }

        UserSnapshot user = userSnapshotRepository.findById(notification.getUserId())
                .orElseThrow(() -> new NotificationException("User not found: " + notification.getUserId()));

        notification.setStatus(NotificationStatus.PENDING);
        notification.incrementRetryCount();
        notification = notificationRepository.save(notification);

        sendNotification(notification, user);

        return notificationMapper.toDTO(notification);
    }

    /**
     * Process scheduled notifications
     */
    @Transactional
    public void processScheduledNotifications() {
        log.info("Processing scheduled notifications");

        List<Notification> scheduledNotifications = notificationRepository
                .findPendingNotificationsScheduledBefore(Instant.now());

        log.info("Found {} scheduled notifications to process", scheduledNotifications.size());

        for (Notification notification : scheduledNotifications) {
            try {
                UserSnapshot user = userSnapshotRepository.findById(notification.getUserId())
                        .orElseThrow(() -> new NotificationException("User not found: " + notification.getUserId()));
                sendNotification(notification, user);
            } catch (Exception e) {
                log.error("Failed to process scheduled notification: {}", notification.getId(), e);
            }
        }
    }

    /**
     * Retry all failed notifications
     */
    @Transactional
    public void retryFailedNotifications() {
        log.info("Retrying failed notifications");

        List<Notification> failedNotifications = notificationRepository.findRetryableFailedNotifications();
        log.info("Found {} failed notifications to retry", failedNotifications.size());

        for (Notification notification : failedNotifications) {
            try {
                retryNotification(notification.getId());
            } catch (Exception e) {
                log.error("Failed to retry notification: {}", notification.getId(), e);
            }
        }
    }

    /**
     * Cancel a pending notification
     */
    @Transactional
    public NotificationDTO cancelNotification(UUID notificationId) {
        log.info("Cancelling notification: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException("Notification not found: " + notificationId));

        if (notification.getStatus() != NotificationStatus.PENDING) {
            throw new NotificationException("Only pending notifications can be cancelled");
        }

        notification.setStatus(NotificationStatus.CANCELLED);
        notification = notificationRepository.save(notification);

        return notificationMapper.toDTO(notification);
    }
}
