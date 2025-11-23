package com.bookfair.notification_service.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.bookfair.notification_service.entity.ReservationSnapshot;
import com.bookfair.notification_service.repository.ReservationSnapshotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka consumer for listening to reservation lifecycle events
 * Updates reservation_snapshot table when reservation events are published
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationEventListener {

    private final ReservationSnapshotRepository reservationSnapshotRepository;

    /**
     * Listen to reservation events from Kafka and update reservation_snapshot
     */
    @KafkaListener(
            topics = "${app.kafka.reservation-events-topic:reservation-events}",
            groupId = "${app.kafka.consumer-group-id:notification-service-reservation-sync}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleReservationEvent(ReservationLifecycleEvent event) {
        try {
            log.info("Received reservation event: {} for reservation ID: {}", 
                    event.getEventType(), event.getReservationId());

            // Find or create reservation snapshot
            ReservationSnapshot snapshot = reservationSnapshotRepository
                    .findById(event.getReservationId())
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
            snapshot.setUpdatedAt(LocalDateTime.now());

            // Save updated snapshot
            reservationSnapshotRepository.save(snapshot);
            log.info("Successfully updated reservation snapshot for reservation ID: {}", 
                    event.getReservationId());

        } catch (Exception e) {
            log.error("Error processing reservation event for reservation ID: {}", 
                    event.getReservationId(), e);
        }
    }
}
