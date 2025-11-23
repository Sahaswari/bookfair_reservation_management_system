package com.bookfair.stall_service.config;

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

import lombok.RequiredArgsConstructor;

import com.bookfair.stall_service.messaging.UserLifecycleEvent;

@Configuration
@EnableKafka
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaConsumerConfig {

    private final UserSyncKafkaProperties userSyncKafkaProperties;

    @Bean
    public ConsumerFactory<String, UserLifecycleEvent> userEventConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> consumerProps = new HashMap<>(
            Objects.requireNonNull(kafkaProperties.buildConsumerProperties(null), "Kafka consumer properties must not be null"));
        JsonDeserializer<UserLifecycleEvent> valueDeserializer = new JsonDeserializer<>(UserLifecycleEvent.class);
        valueDeserializer.addTrustedPackages("*");
        valueDeserializer.setRemoveTypeHeaders(true);
        valueDeserializer.setUseTypeMapperForKey(false);
        return new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ProducerFactory<String, Object> userEventProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> producerProps = new HashMap<>(
                Objects.requireNonNull(kafkaProperties.buildProducerProperties(null), "Kafka producer properties must not be null"));
        return new DefaultKafkaProducerFactory<>(producerProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> userEventProducerFactory) {
        return new KafkaTemplate<>(Objects.requireNonNull(userEventProducerFactory, "producer factory must not be null"));
    }

    @Bean
        public ConcurrentKafkaListenerContainerFactory<String, UserLifecycleEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, UserLifecycleEvent> userEventConsumerFactory,
            KafkaTemplate<String, Object> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, UserLifecycleEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(Objects.requireNonNull(userEventConsumerFactory, "consumer factory must not be null"));
        factory.setConcurrency(userSyncKafkaProperties.getListenerConcurrency());
        ContainerProperties containerProperties = factory.getContainerProperties();
        containerProperties.setAckMode(ContainerProperties.AckMode.RECORD);
        containerProperties.setMissingTopicsFatal(false);

        UserSyncKafkaProperties.Retry retry = userSyncKafkaProperties.getRetry();
        ExponentialBackOffWithMaxRetries backoff = new ExponentialBackOffWithMaxRetries(Math.max(retry.getMaxAttempts() - 1, 1));
        backoff.setInitialInterval(retry.getInitialIntervalMs());
        backoff.setMultiplier(retry.getMultiplier());
        backoff.setMaxInterval(retry.getMaxIntervalMs());

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
            Objects.requireNonNull(kafkaTemplate, "KafkaTemplate must not be null"),
            (record, exception) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, backoff));
        return factory;
    }

    @Bean
    public NewTopic userEventsTopic() {
        String topicName = Objects.requireNonNull(userSyncKafkaProperties.getUserEventsTopic(),
                "user-events topic must be configured");
        return TopicBuilder.name(topicName)
                .partitions(3)
                .replicas(1)
                .build();
    }

        @Bean
        public NewTopic userEventsDltTopic() {
        String topicName = Objects.requireNonNull(userSyncKafkaProperties.getUserEventsTopic(),
            "user-events topic must be configured");
        return TopicBuilder.name(topicName + ".DLT")
            .partitions(3)
            .replicas(1)
            .build();
        }
}
