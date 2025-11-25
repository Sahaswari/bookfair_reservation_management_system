package com.bookfair.reservation_service.messaging;

import com.bookfair.reservation_service.service.StallSnapshotSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Consumes stall lifecycle events emitted by the Stall Service and keeps the local
 * stall_snapshot table up-to-date.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class StallEventListener {

    private final StallSnapshotSyncService stallSnapshotSyncService;

    @KafkaListener(
            topics = "${app.kafka.stall-events-topic:stall-events}",
            groupId = "${app.kafka.consumer-group-id:reservation-service-stall-sync}",
            containerFactory = "stallKafkaListenerContainerFactory",
            autoStartup = "${app.kafka.enabled:false}"
    )
    public void handleStallLifecycleEvent(StallLifecycleEvent event,
                                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                          @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                          @Header(KafkaHeaders.OFFSET) long offset) {
        if (event == null || event.getStallId() == null) {
            log.warn("Received malformed stall lifecycle event from topic {} partition {} offset {}", topic, partition, offset);
            return;
        }

        log.info("Consuming stall lifecycle event {} from {}[{}:{}]", event.getEventType(), topic, partition, offset);
        String eventType = event.getEventType() != null ? event.getEventType().toUpperCase() : "";
        
        if ("STALL_DELETED".equals(eventType)) {
            stallSnapshotSyncService.deleteStallSnapshot(event.getStallId());
            return;
        }

        stallSnapshotSyncService.createOrUpdateStallSnapshot(event);
    }

    @DltHandler
    public void handleDeadLetter(StallLifecycleEvent event,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.OFFSET) long offset) {
        log.error("Stall lifecycle event for stall {} routed to dead-letter topic {} at offset {}. Manual intervention required.",
                event != null ? event.getStallId() : null, topic, offset);
    }
}
