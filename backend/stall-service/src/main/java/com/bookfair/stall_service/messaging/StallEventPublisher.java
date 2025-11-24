package com.bookfair.stall_service.messaging;

import com.bookfair.stall_service.entity.Stall;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Publishes stall lifecycle events to Kafka.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StallEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.stall-events-topic:stall-events}")
    private String stallEventsTopic;

    @Value("${app.kafka.enabled:false}")
    private boolean kafkaEnabled;

    public void publishStallCreated(Stall stall) {
        publishEvent(stall, "STALL_CREATED");
    }

    public void publishStallUpdated(Stall stall) {
        publishEvent(stall, "STALL_UPDATED");
    }

    public void publishStallDeleted(Stall stall) {
        publishEvent(stall, "STALL_DELETED");
    }

    private void publishEvent(Stall stall, String eventType) {
        if (!kafkaEnabled) {
            log.debug("Kafka is disabled. Skipping stall event publication: {}", eventType);
            return;
        }

        StallLifecycleEvent event = StallLifecycleEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .occurredAt(Instant.now())
                .stallId(stall.getId())
                .bookFairEventId(stall.getEvent().getId())
                .stallCode(stall.getStallCode())
                .sizeCategory(stall.getSizeCategory().name())
                .price(stall.getPrice())
                .locationX(stall.getLocationX())
                .locationY(stall.getLocationY())
                .isReserved(stall.getIsReserved())
                .reservedBy(stall.getReservedBy())
                .build();

        log.info("Publishing stall event: {} for stallId: {}", eventType, stall.getId());
        
        try {
            kafkaTemplate.send(stallEventsTopic, stall.getId().toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish stall event: {}", eventType, e);
            // We might want to throw an exception here depending on reliability requirements,
            // but for now we'll just log it to avoid breaking the main transaction if Kafka is down.
            // In a robust system, we might use the Outbox Pattern.
        }
    }
}
