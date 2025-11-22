package com.bookfair.auth_service.config;

import java.util.Objects;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import lombok.RequiredArgsConstructor;

/**
 * Creates Kafka topics required by the Auth Service when Kafka integration is enabled.
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaTopicConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public NewTopic userEventsTopic() {
        String topicName = Objects.requireNonNull(kafkaProperties.getUserEventsTopic(), "user-events topic must be configured");
        return TopicBuilder
            .name(topicName)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
