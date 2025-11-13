package com.bookfair.auth_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private String allowedOrigins = "*";

    public List<String> resolvedAllowedOrigins() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(entry -> !entry.isEmpty())
                .collect(Collectors.toList());
    }
}

