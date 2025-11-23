package com.bookfair.auth_service.service.impl;

import com.bookfair.auth_service.config.PasswordResetProperties;
import com.bookfair.auth_service.entity.User;
import com.bookfair.auth_service.service.PasswordResetNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoggingPasswordResetNotificationService implements PasswordResetNotificationService {

    private final PasswordResetProperties passwordResetProperties;

    @Override
    public void sendPasswordResetEmail(User user, String resetLink, Instant expiresAt) {
        if (!passwordResetProperties.isEnabled()) {
            log.debug("Password reset notifications disabled; request for {} suppressed", user.getEmail());
            return;
        }
        log.info("Password reset email requested for {}. Link: {} (expires at {})",
                user.getEmail(), resetLink, expiresAt);
    }
}
