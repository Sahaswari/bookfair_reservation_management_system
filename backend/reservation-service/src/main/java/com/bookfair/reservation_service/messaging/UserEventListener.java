package com.bookfair.reservation_service.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.bookfair.reservation_service.service.UserSnapshotSyncService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class UserEventListener {

    private final UserSnapshotSyncService userSnapshotSyncService;

    @KafkaListener(
        topics = "${app.kafka.user-events-topic:user-events}",
        groupId = "${app.kafka.consumer-group-id:reservation-service-user-sync}",
        autoStartup = "${app.kafka.enabled:false}"
    )
    public void handleUserLifecycleEvent(UserLifecycleEvent event,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                         @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                         @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Reservation service consumed user event {} from {}[{}:{}]",
            event != null ? event.getEventType() : null, topic, partition, offset);
        userSnapshotSyncService.handle(event);
    }

    @DltHandler
    public void handleDeadLetter(UserLifecycleEvent event,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.OFFSET) long offset) {
        log.error("User lifecycle event for user {} routed to reservation-service DLT topic {} at offset {}",
            event != null ? event.getUserId() : null, topic, offset);
    }
}
