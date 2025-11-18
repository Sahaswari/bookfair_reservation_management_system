package com.bookfair.auth_service.messaging;

import com.bookfair.auth_service.config.KafkaProperties;
import com.bookfair.auth_service.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    public void publishUserRegistered(User user) {
        if (!kafkaProperties.isEnabled()) {
            log.debug("Kafka disabled, skipping user registered event");
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", user.getId().toString());
        payload.put("email", user.getEmail());
        payload.put("role", user.getRole().name());
        payload.put("status", user.getStatus().name());
        kafkaTemplate.send(kafkaProperties.getUserEventsTopic(), payload);
        log.info("Published user registered event for {}", user.getEmail());
    }
}

