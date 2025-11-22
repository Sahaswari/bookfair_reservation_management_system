package com.bookfair.stall_service.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.bookfair.stall_service.dto.UserSnapshotDTO;
import com.bookfair.stall_service.service.UserSnapshotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consumes user lifecycle events emitted by the Auth Service and keeps the local
 * user_snapshot table up-to-date. Messages are retried automatically; if all retry
 * attempts fail, they are forwarded to the dead-letter topic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class UserEventListener {

    private final UserSnapshotService userSnapshotService;

    @KafkaListener(
            topics = "${app.kafka.user-events-topic:user-events}",
            groupId = "${app.kafka.consumer-group-id:stall-service-user-sync}",
            autoStartup = "${app.kafka.enabled:false}"
    )
    public void handleUserLifecycleEvent(UserLifecycleEvent event,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                         @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                         @Header(KafkaHeaders.OFFSET) long offset) {
        if (event == null || event.getUserId() == null) {
            log.warn("Received malformed user lifecycle event from topic {} partition {} offset {}", topic, partition, offset);
            return;
        }

        log.info("Consuming user lifecycle event {} from {}[{}:{}]", event.getEventType(), topic, partition, offset);
        UserSnapshotDTO dto = new UserSnapshotDTO();
        dto.setUserId(event.getUserId());
        dto.setFirstName(event.getFirstName());
        dto.setLastName(event.getLastName());
        dto.setEmail(event.getEmail());
        dto.setRole(event.getRole());
        dto.setStatus(event.getStatus());
        userSnapshotService.createOrUpdateUserSnapshot(dto);
    }

    @DltHandler
    public void handleDeadLetter(UserLifecycleEvent event,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.OFFSET) long offset) {
        log.error("User lifecycle event for user {} routed to dead-letter topic {} at offset {}. Manual intervention required.",
                event != null ? event.getUserId() : null, topic, offset);
    }
}
