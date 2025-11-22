package com.bookfair.auth_service.messaging;

import com.bookfair.auth_service.config.KafkaProperties;
import com.bookfair.auth_service.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    public void publishUserRegistered(User user) {
        publishEvent(user, "USER_CREATED", "registered");
    }

    public void publishUserUpdated(User user) {
        publishEvent(user, "USER_UPDATED", "updated");
    }

    public void publishUserDeleted(User user) {
        publishEvent(user, "USER_DELETED", "deleted");
    }

    private void publishEvent(User user, String eventType, String action) {
        if (!kafkaProperties.isEnabled()) {
            log.debug("Kafka disabled, skipping user {} event", action);
            return;
        }

        UserLifecycleEvent event = UserLifecycleEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .occurredAt(Instant.now())
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .companyName(user.getCompanyName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .build();

        String topic = Objects.requireNonNull(kafkaProperties.getUserEventsTopic(), "user-events topic must be configured");
        String key = event.getUserId() != null ? event.getUserId().toString() : event.getEventId().toString();

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                topic,
                Objects.requireNonNull(key, "event key must not be null"),
                event);

        future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("Failed to publish user {} event for {}", action, user.getEmail(), throwable);
            } else {
                log.info("Published user {} event for {} to partition {} with offset {}",
                        action,
                        user.getEmail(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}

