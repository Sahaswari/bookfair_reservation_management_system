package com.bookfair.notification_service.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.bookfair.notification_service.messaging.ReservationLifecycleEvent;

/**
 * Kafka configuration for reservation events
 * Enables consumption of reservation lifecycle events from reservation-service
 */
@Configuration
@EnableKafka
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class ReservationKafkaConfig {

    /**
     * Consumer factory for ReservationLifecycleEvent
     */
    @Bean(name = "reservationEventConsumerFactory")
    public ConsumerFactory<String, ReservationLifecycleEvent> reservationEventConsumerFactory(
            KafkaProperties kafkaProperties) {
        Map<String, Object> consumerProps = new HashMap<>(
            Objects.requireNonNull(kafkaProperties.buildConsumerProperties(null), 
                "Kafka consumer properties must not be null"));
        
        JsonDeserializer<ReservationLifecycleEvent> deserializer = 
            new JsonDeserializer<>(ReservationLifecycleEvent.class);
        deserializer.addTrustedPackages("*");
        deserializer.setRemoveTypeHeaders(false);
        deserializer.setUseTypeMapperForKey(false);
        
        return new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), deserializer);
    }

    /**
     * Kafka listener container factory for reservation events
     * Configured with retry policy, error handling, and dead letter topic
     */
    @Bean(name = "reservationKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, ReservationLifecycleEvent> reservationKafkaListenerContainerFactory(
            ConsumerFactory<String, ReservationLifecycleEvent> reservationEventConsumerFactory,
            KafkaTemplate<String, Object> kafkaTemplate) {
        
        ConcurrentKafkaListenerContainerFactory<String, ReservationLifecycleEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(Objects.requireNonNull(reservationEventConsumerFactory, 
            "consumer factory must not be null"));
        factory.setConcurrency(3);
        
        ContainerProperties containerProperties = factory.getContainerProperties();
        containerProperties.setAckMode(ContainerProperties.AckMode.RECORD);
        containerProperties.setMissingTopicsFatal(false);

        // Retry configuration
        ExponentialBackOffWithMaxRetries backoff = new ExponentialBackOffWithMaxRetries(2);
        backoff.setInitialInterval(1000);
        backoff.setMultiplier(2.0);
        backoff.setMaxInterval(10000);

        // Dead letter topic for failed messages
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
            Objects.requireNonNull(kafkaTemplate, "KafkaTemplate must not be null"),
            (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
        
        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, backoff));
        return factory;
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

    /**
     * Create reservation events dead letter topic
     */
    @Bean
    public NewTopic reservationEventsDltTopic() {
        return TopicBuilder.name("reservation-events.DLT")
            .partitions(3)
            .replicas(1)
            .config("retention.ms", "1209600000")  // 14 days retention
            .config("min.insync.replicas", "1")
            .build();
    }
}
