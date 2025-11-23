package com.bookfair.notification_service.service;

import com.bookfair.notification_service.entity.ReservationSnapshot;
import com.bookfair.notification_service.messaging.ReservationLifecycleEvent;
import com.bookfair.notification_service.repository.ReservationSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service for syncing reservation snapshots from Kafka events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationSnapshotSyncService {

    private final ReservationSnapshotRepository reservationSnapshotRepository;

    @Transactional
    public void handle(ReservationLifecycleEvent event) {
        if (event == null || event.getReservationId() == null) {
            log.warn("Ignoring reservation lifecycle event without reservationId: {}", event);
            return;
        }

        log.info("Syncing reservation snapshot for reservation: {}", event.getReservationId());

        // Find or create reservation snapshot
        ReservationSnapshot snapshot = reservationSnapshotRepository
                .findById(event.getReservationId())
                .orElseGet(() -> {
                    log.info("Creating new reservation snapshot for reservation ID: {}", 
                            event.getReservationId());
                    ReservationSnapshot newSnapshot = new ReservationSnapshot();
                    newSnapshot.setReservationId(event.getReservationId());
                    return newSnapshot;
                });

        // Update snapshot with event data
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

        reservationSnapshotRepository.save(snapshot);
        log.info("Successfully synced reservation snapshot for reservation: {}", event.getReservationId());
    }
}
