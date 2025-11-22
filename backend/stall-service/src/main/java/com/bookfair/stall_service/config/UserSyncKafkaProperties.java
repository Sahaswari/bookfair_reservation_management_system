package com.bookfair.stall_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Application-specific Kafka knobs for synchronising user snapshots.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.kafka")
public class UserSyncKafkaProperties {

    private boolean enabled = false;

    @NotBlank
    private String userEventsTopic = "user-events";

    @NotBlank
    private String consumerGroupId = "stall-service-user-sync";

    @Min(1)
    private int listenerConcurrency = 1;

    private final Retry retry = new Retry();

    @Getter
    @Setter
    public static class Retry {
        @Min(1)
        private int maxAttempts = 5;
        @Min(100)
        private long initialIntervalMs = 1_000L;
        @Min(100)
        private long maxIntervalMs = 60_000L;
        private double multiplier = 2.0d;
    }
}
