package com.bookfair.notification_service.scheduler;

import com.bookfair.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks for notification processing
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;

    /**
     * Process scheduled notifications every 5 minutes
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void processScheduledNotifications() {
        log.debug("Running scheduled notification processing task");
        try {
            notificationService.processScheduledNotifications();
        } catch (Exception e) {
            log.error("Error in scheduled notification processing", e);
        }
    }

    /**
     * Retry failed notifications every 30 minutes
     */
    @Scheduled(fixedDelay = 1800000) // 30 minutes
    public void retryFailedNotifications() {
        log.debug("Running failed notification retry task");
        try {
            notificationService.retryFailedNotifications();
        } catch (Exception e) {
            log.error("Error in failed notification retry", e);
        }
    }
}
