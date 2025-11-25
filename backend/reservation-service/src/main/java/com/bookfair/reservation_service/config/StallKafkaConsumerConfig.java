package com.bookfair.reservation_service.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.bookfair.reservation_service.messaging.StallLifecycleEvent;

@Configuration
@EnableKafka
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class StallKafkaConsumerConfig {

    private final UserSyncKafkaProperties userSyncKafkaProperties;

    public StallKafkaConsumerConfig(UserSyncKafkaProperties userSyncKafkaProperties) {
        this.userSyncKafkaProperties = userSyncKafkaProperties;
    }

    @Bean
    public ConsumerFactory<String, StallLifecycleEvent> stallEventConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> consumerProps = new HashMap<>(
            Objects.requireNonNull(kafkaProperties.buildConsumerProperties(null), "Kafka consumer properties must not be null"));
        JsonDeserializer<StallLifecycleEvent> deserializer = new JsonDeserializer<>(StallLifecycleEvent.class);
        deserializer.addTrustedPackages("*");
        deserializer.setRemoveTypeHeaders(true);
        deserializer.setUseTypeMapperForKey(false);
        return new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StallLifecycleEvent> stallKafkaListenerContainerFactory(
            ConsumerFactory<String, StallLifecycleEvent> stallEventConsumerFactory,
            KafkaTemplate<String, Object> kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, StallLifecycleEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(Objects.requireNonNull(stallEventConsumerFactory, "consumer factory must not be null"));
        factory.setConcurrency(userSyncKafkaProperties.getListenerConcurrency());
        ContainerProperties containerProperties = factory.getContainerProperties();
        containerProperties.setAckMode(ContainerProperties.AckMode.RECORD);
        containerProperties.setMissingTopicsFatal(false);

        UserSyncKafkaProperties.Retry retry = userSyncKafkaProperties.getRetry();
        ExponentialBackOffWithMaxRetries backoff = new ExponentialBackOffWithMaxRetries(
            Math.max(retry.getMaxAttempts() - 1, 1));
        backoff.setInitialInterval(retry.getInitialIntervalMs());
        backoff.setMultiplier(retry.getMultiplier());
        backoff.setMaxInterval(retry.getMaxIntervalMs());

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
            Objects.requireNonNull(kafkaTemplate, "KafkaTemplate must not be null"),
            (record, exception) -> new org.apache.kafka.common.TopicPartition(record.topic() + ".DLT", record.partition()));
        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, backoff));
        return factory;
    }
}
