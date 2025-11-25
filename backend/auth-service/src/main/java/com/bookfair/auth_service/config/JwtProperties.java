package com.bookfair.auth_service.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    @NotBlank(message = "JWT secret must be provided")
    private String secret;

    @Positive(message = "Access token validity must be positive")
    private long accessTokenValidity;

    @Positive(message = "Refresh token validity must be positive")
    private long refreshTokenValidity;
}

