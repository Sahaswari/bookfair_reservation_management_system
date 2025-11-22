package com.bookfair.auth_service.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.Instant;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.password-reset")
public class PasswordResetProperties {

    private boolean enabled = true;

    @DurationMin(minutes = 1, message = "Password reset token validity must be at least 1 minute")
    private Duration tokenValidity = Duration.ofMinutes(30);

    @NotBlank(message = "Password reset frontend URL must be configured")
    private String frontendBaseUrl = "http://localhost:4200/reset-password";

    public Instant calculateExpiryFrom(Instant now) {
        return now.plus(tokenValidity);
    }
}
