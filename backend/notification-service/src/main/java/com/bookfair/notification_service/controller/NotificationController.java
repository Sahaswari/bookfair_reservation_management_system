package com.bookfair.notification_service.controller;

import com.bookfair.notification_service.dto.ApiResponse;
import com.bookfair.notification_service.dto.NotificationDTO;
import com.bookfair.notification_service.dto.NotificationRequest;
import com.bookfair.notification_service.entity.NotificationStatus;
import com.bookfair.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Notification operations
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Create a new notification
     * POST /api/notifications
     */
    @PostMapping
    public ResponseEntity<ApiResponse<NotificationDTO>> createNotification(
            @Valid @RequestBody NotificationRequest request) {
        try {
            log.info("Creating notification for user: {}", request.getUserId());
            NotificationDTO notification = notificationService.createNotification(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Notification created successfully", notification));
        } catch (Exception e) {
            log.error("Error creating notification", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get notification by ID
     * GET /api/notifications/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationDTO>> getNotificationById(@PathVariable UUID id) {
        try {
            NotificationDTO notification = notificationService.getNotificationById(id);
            return ResponseEntity.ok(ApiResponse.success("Notification retrieved successfully", notification));
        } catch (Exception e) {
            log.error("Error retrieving notification", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get notifications by user ID
     * GET /api/notifications/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotificationsByUserId(
            @PathVariable UUID userId) {
        try {
            List<NotificationDTO> notifications = notificationService.getNotificationsByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
        } catch (Exception e) {
            log.error("Error retrieving notifications for user", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get notifications by reservation ID
     * GET /api/notifications/reservation/{reservationId}
     */
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotificationsByReservationId(
            @PathVariable UUID reservationId) {
        try {
            List<NotificationDTO> notifications = notificationService.getNotificationsByReservationId(reservationId);
            return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
        } catch (Exception e) {
            log.error("Error retrieving notifications for reservation", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get notifications by status
     * GET /api/notifications/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getNotificationsByStatus(
            @PathVariable NotificationStatus status) {
        try {
            List<NotificationDTO> notifications = notificationService.getNotificationsByStatus(status);
            return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
        } catch (Exception e) {
            log.error("Error retrieving notifications by status", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Retry a failed notification
     * POST /api/notifications/{id}/retry
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<ApiResponse<NotificationDTO>> retryNotification(@PathVariable UUID id) {
        try {
            log.info("Retrying notification: {}", id);
            NotificationDTO notification = notificationService.retryNotification(id);
            return ResponseEntity.ok(ApiResponse.success("Notification retry initiated", notification));
        } catch (Exception e) {
            log.error("Error retrying notification", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Cancel a pending notification
     * POST /api/notifications/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<NotificationDTO>> cancelNotification(@PathVariable UUID id) {
        try {
            log.info("Cancelling notification: {}", id);
            NotificationDTO notification = notificationService.cancelNotification(id);
            return ResponseEntity.ok(ApiResponse.success("Notification cancelled successfully", notification));
        } catch (Exception e) {
            log.error("Error cancelling notification", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Process scheduled notifications (Admin endpoint)
     * POST /api/notifications/process-scheduled
     */
    @PostMapping("/process-scheduled")
    public ResponseEntity<ApiResponse<Void>> processScheduledNotifications() {
        try {
            log.info("Processing scheduled notifications");
            notificationService.processScheduledNotifications();
            return ResponseEntity.ok(ApiResponse.success("Scheduled notifications processed", null));
        } catch (Exception e) {
            log.error("Error processing scheduled notifications", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Retry all failed notifications (Admin endpoint)
     * POST /api/notifications/retry-failed
     */
    @PostMapping("/retry-failed")
    public ResponseEntity<ApiResponse<Void>> retryFailedNotifications() {
        try {
            log.info("Retrying all failed notifications");
            notificationService.retryFailedNotifications();
            return ResponseEntity.ok(ApiResponse.success("Failed notifications retry initiated", null));
        } catch (Exception e) {
            log.error("Error retrying failed notifications", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
