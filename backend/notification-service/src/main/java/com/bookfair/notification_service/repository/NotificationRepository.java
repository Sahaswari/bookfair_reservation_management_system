package com.bookfair.notification_service.repository;

import com.bookfair.notification_service.entity.Notification;
import com.bookfair.notification_service.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Find all notifications for a specific user
     */
    List<Notification> findByUserId(UUID userId);

    /**
     * Find all notifications for a specific reservation
     */
    List<Notification> findByReservationId(UUID reservationId);

    /**
     * Find all notifications by status
     */
    List<Notification> findByStatus(NotificationStatus status);

    /**
     * Find all pending notifications scheduled before or at the given time
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.scheduledFor <= :scheduledFor")
    List<Notification> findPendingNotificationsScheduledBefore(@Param("scheduledFor") Instant scheduledFor);

    /**
     * Find failed notifications that can be retried
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND n.retryCount < n.maxRetries")
    List<Notification> findRetryableFailedNotifications();

    /**
     * Find notifications by user and status
     */
    List<Notification> findByUserIdAndStatus(UUID userId, NotificationStatus status);
}
