package com.bookfair.reservation_service.service;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookfair.reservation_service.entity.UserSnapshot;
import com.bookfair.reservation_service.messaging.UserLifecycleEvent;
import com.bookfair.reservation_service.repository.UserSnapshotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSnapshotSyncService {

    private final UserSnapshotRepository userSnapshotRepository;

    @Transactional
    public void handle(UserLifecycleEvent event) {
        if (event == null || event.getUserId() == null) {
            log.warn("Ignoring user lifecycle event without userId: {}", event);
            return;
        }

        String eventType = Optional.ofNullable(event.getEventType())
            .map(type -> type.toUpperCase(Locale.ROOT))
            .orElse("USER_UPDATED");

        if ("USER_DELETED".equals(eventType)) {
            deleteSnapshot(event.getUserId());
        } else {
            upsertSnapshot(event);
        }
    }

    private void upsertSnapshot(UserLifecycleEvent event) {
        UserSnapshot snapshot = userSnapshotRepository.findById(event.getUserId())
            .orElseGet(() -> {
                UserSnapshot created = new UserSnapshot();
                created.setUserId(event.getUserId());
                return created;
            });
        snapshot.setFirstName(event.getFirstName());
        snapshot.setLastName(event.getLastName());
        snapshot.setEmail(event.getEmail());
        snapshot.setMobileNo(event.getMobileNo());
        snapshot.setRole(event.getRole());
        snapshot.setStatus(event.getStatus());
        userSnapshotRepository.save(snapshot);
        log.debug("Upserted user snapshot for {}", event.getUserId());
    }

    private void deleteSnapshot(UUID userId) {
        if (!userSnapshotRepository.existsById(userId)) {
            log.debug("User snapshot for {} not found, nothing to delete", userId);
            return;
        }
        userSnapshotRepository.deleteById(userId);
        log.debug("Deleted user snapshot for {}", userId);
    }
}
