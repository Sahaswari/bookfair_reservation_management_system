package com.bookfair.reservation_service.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka producer for publishing reservation lifecycle events
 */
@Component
@Slf4j
public class ReservationEventProducer {

    private final KafkaTemplate<String, ReservationLifecycleEvent> kafkaTemplate;
    private final String reservationEventsTopic;
    private final boolean kafkaEnabled;

    public ReservationEventProducer(
            @Qualifier("reservationEventKafkaTemplate") 
            KafkaTemplate<String, ReservationLifecycleEvent> kafkaTemplate,
            @Value("${app.kafka.reservation-events-topic:reservation-events}")
            String reservationEventsTopic,
            @Value("${app.kafka.enabled:true}")
            boolean kafkaEnabled) {
        this.kafkaTemplate = kafkaTemplate;
        this.reservationEventsTopic = reservationEventsTopic;
        this.kafkaEnabled = kafkaEnabled;
    }

    /**
     * Publish reservation event to Kafka
     */
    public void publishReservationEvent(ReservationLifecycleEvent event) {
        if (!kafkaEnabled) {
            log.debug("Kafka is disabled, skipping event publication");
            return;
        }

        try {
            log.info("Publishing reservation event: {} for reservation ID: {}", 
                    event.getEventType(), event.getReservationId());
            
            kafkaTemplate.send(reservationEventsTopic, 
                    event.getReservationId().toString(), 
                    event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Successfully published {} event for reservation: {}", 
                                    event.getEventType(), event.getReservationId());
                        } else {
                            log.error("Failed to publish {} event for reservation: {}", 
                                    event.getEventType(), event.getReservationId(), ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error publishing reservation event", e);
        }
    }
}
