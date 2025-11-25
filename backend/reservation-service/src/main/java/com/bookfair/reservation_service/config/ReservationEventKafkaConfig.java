package com.bookfair.reservation_service.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.bookfair.reservation_service.messaging.ReservationLifecycleEvent;

/**
 * Kafka producer configuration for reservation events
 * Enables publishing of reservation lifecycle events to Kafka topic
 */
@Configuration
@EnableKafka
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class ReservationEventKafkaConfig {

    /**
     * Producer factory for ReservationLifecycleEvent
     */
    @Bean(name = "reservationEventProducerFactory")
    public ProducerFactory<String, ReservationLifecycleEvent> reservationEventProducerFactory(
            KafkaProperties kafkaProperties) {
        Map<String, Object> producerProps = new HashMap<>(
            Objects.requireNonNull(kafkaProperties.buildProducerProperties(null), 
                "Kafka producer properties must not be null"));
        
        return new DefaultKafkaProducerFactory<>(producerProps);
    }

    /**
     * Kafka template for sending reservation events
     */
    @Bean(name = "reservationEventKafkaTemplate")
    public KafkaTemplate<String, ReservationLifecycleEvent> reservationEventKafkaTemplate(
            ProducerFactory<String, ReservationLifecycleEvent> reservationEventProducerFactory) {
        return new KafkaTemplate<>(Objects.requireNonNull(reservationEventProducerFactory, 
            "producer factory must not be null"));
    }

    /**
     * Create reservation events topic if it doesn't exist
     */
    @Bean
    public NewTopic reservationEventsTopic() {
        return TopicBuilder.name("reservation-events")
            .partitions(3)
            .replicas(1)
            .config("retention.ms", "604800000")  // 7 days retention
            .config("min.insync.replicas", "1")
            .build();
    }
}
