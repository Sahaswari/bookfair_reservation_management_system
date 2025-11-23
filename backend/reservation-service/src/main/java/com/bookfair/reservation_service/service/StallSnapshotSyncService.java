package com.bookfair.reservation_service.service;

import com.bookfair.reservation_service.entity.StallSnapshot;
import com.bookfair.reservation_service.messaging.StallLifecycleEvent;
import com.bookfair.reservation_service.repository.StallSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service to sync stall snapshots from Stall Service events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StallSnapshotSyncService {

    private final StallSnapshotRepository stallSnapshotRepository;

    @Transactional
    public void createOrUpdateStallSnapshot(StallLifecycleEvent event) {
        log.info("Syncing stall snapshot for stallId: {}", event.getStallId());

        StallSnapshot snapshot = stallSnapshotRepository.findById(event.getStallId())
                .orElse(new StallSnapshot());

        snapshot.setStallId(event.getStallId());
        snapshot.setEventId(event.getBookFairEventId());
        snapshot.setStallCode(event.getStallCode());
        snapshot.setSizeCategory(event.getSizeCategory());
        snapshot.setPrice(event.getPrice());
        snapshot.setLocationX(event.getLocationX());
        snapshot.setLocationY(event.getLocationY());

        stallSnapshotRepository.save(snapshot);
        log.info("Stall snapshot synced successfully for stallId: {}", event.getStallId());
    }

    @Transactional
    public void deleteStallSnapshot(UUID stallId) {
        log.info("Deleting stall snapshot for stallId: {}", stallId);
        if (stallSnapshotRepository.existsById(stallId)) {
            stallSnapshotRepository.deleteById(stallId);
            log.info("Stall snapshot deleted successfully for stallId: {}", stallId);
        } else {
            log.warn("Stall snapshot not found for deletion, stallId: {}", stallId);
        }
    }
}
