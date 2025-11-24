package com.bookfair.notification_service.messaging;

import java.time.Instant;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.bookfair.notification_service.entity.ReservationSnapshot;
import com.bookfair.notification_service.repository.ReservationSnapshotRepository;
import com.bookfair.notification_service.service.ReservationNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka consumer for listening to reservation lifecycle events
 * Updates reservation_snapshot table when reservation events are published
 * Triggers notifications based on event type
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationEventListener {

    private final ReservationSnapshotRepository reservationSnapshotRepository;
    private final ReservationNotificationService reservationNotificationService;

    /**
     * Listen to reservation events from Kafka and update reservation_snapshot
     */
    @KafkaListener(
            topics = "${app.kafka.reservation-events-topic:reservation-events}",
            groupId = "${app.kafka.consumer-group-id:notification-service-reservation-sync}",
            containerFactory = "reservationKafkaListenerContainerFactory"
    )
    public void handleReservationEvent(ReservationLifecycleEvent event) {
        try {
            log.info("Received reservation event: {} for reservation ID: {}", 
                    event.getEventType(), event.getReservationId());

            // Find or create reservation snapshot
                ReservationSnapshot snapshot = reservationSnapshotRepository
                    .findByReservationId(event.getReservationId())
                    .orElseGet(() -> {
                        log.info("Creating new reservation snapshot for reservation ID: {}", 
                                event.getReservationId());
                        return new ReservationSnapshot();
                    });

            // Update reservation snapshot with latest data
            snapshot.setReservationId(event.getReservationId());
            snapshot.setUserId(event.getUserId());
            snapshot.setStallId(event.getStallId());
            snapshot.setEventId(event.getBookFairEventId());
            snapshot.setStatus(event.getStatus());
            snapshot.setReservationDate(event.getReservationDate());
            snapshot.setConfirmationCode(event.getConfirmationCode());
            snapshot.setQrCodeUrl(event.getQrCodeUrl());
            
            // Update user snapshot fields
            snapshot.setUserFirstName(event.getUserFirstName());
            snapshot.setUserLastName(event.getUserLastName());
            snapshot.setUserEmail(event.getUserEmail());
            snapshot.setUserRole(event.getUserRole());
            snapshot.setUserStatus(event.getUserStatus());
            
            // Update stall snapshot fields
            snapshot.setStallCode(event.getStallCode());
            snapshot.setSizeCategory(event.getSizeCategory());
            snapshot.setPrice(event.getPrice());
            snapshot.setLocationX(event.getLocationX());
            snapshot.setLocationY(event.getLocationY());
            
            snapshot.setUpdatedAt(Instant.now());

            // Save updated snapshot
            reservationSnapshotRepository.save(snapshot);
            log.info("Successfully updated reservation snapshot for reservation ID: {}", 
                    event.getReservationId());

            // Trigger notifications based on event type
            triggerNotification(event);

        } catch (Exception e) {
            log.error("Error processing reservation event for reservation ID: {}", 
                    event.getReservationId(), e);
        }
    }

    /**
     * Trigger appropriate notification based on event type
     */
    private void triggerNotification(ReservationLifecycleEvent event) {
        try {
            switch (event.getEventType()) {
                case "RESERVATION_CREATED" -> 
                    reservationNotificationService.sendReservationCreatedNotification(event.getReservationId());
                case "RESERVATION_CONFIRMED" -> 
                    reservationNotificationService.sendReservationConfirmedNotification(event.getReservationId());
                case "RESERVATION_CANCELLED" -> 
                    reservationNotificationService.sendReservationCancelledNotification(event.getReservationId());
                default -> log.debug("No notification configured for event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to send notification for reservation: {}", event.getReservationId(), e);
            // Don't throw exception to avoid Kafka retry loop
        }
    }
}
