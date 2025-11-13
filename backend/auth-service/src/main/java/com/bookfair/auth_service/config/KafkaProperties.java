package com.bookfair.auth_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaProperties {

    private boolean enabled = false;
    private String userEventsTopic = "user-events";
}

