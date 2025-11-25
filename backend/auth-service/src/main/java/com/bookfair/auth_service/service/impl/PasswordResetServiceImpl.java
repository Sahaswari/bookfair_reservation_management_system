package com.bookfair.auth_service.service.impl;

import com.bookfair.auth_service.config.PasswordResetProperties;
import com.bookfair.auth_service.dto.ForgotPasswordRequest;
import com.bookfair.auth_service.dto.ResetPasswordRequest;
import com.bookfair.auth_service.entity.PasswordResetToken;
import com.bookfair.auth_service.entity.User;
import com.bookfair.auth_service.entity.UserSession;
import com.bookfair.auth_service.exception.InvalidTokenException;
import com.bookfair.auth_service.repository.PasswordResetTokenRepository;
import com.bookfair.auth_service.repository.UserRepository;
import com.bookfair.auth_service.repository.UserSessionRepository;
import com.bookfair.auth_service.service.PasswordResetNotificationService;
import com.bookfair.auth_service.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetNotificationService passwordResetNotificationService;
    private final PasswordResetProperties passwordResetProperties;
    private final UserSessionRepository userSessionRepository;

    @Override
    @Transactional
    public void sendResetInstructions(ForgotPasswordRequest request) {
        if (!passwordResetProperties.isEnabled()) {
            log.warn("Password reset feature disabled; ignoring forgot-password request");
            return;
        }

        Instant now = Instant.now();
        passwordResetTokenRepository.deleteByExpiresAtBefore(now);
        String email = request.getEmail().trim();
        userRepository.findByEmailIgnoreCase(email)
            .ifPresentOrElse(user -> handlePasswordResetRequest(user, now),
                () -> log.debug("Password reset requested for non-existent email {}", email));
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!passwordResetProperties.isEnabled()) {
            throw new InvalidTokenException("Password reset feature is disabled");
        }

        Instant now = Instant.now();
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

        if (token.isUsed() || token.getExpiresAt().isBefore(now)) {
            throw new InvalidTokenException("Invalid or expired password reset token");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsed(true);
        token.setUsedAt(now);
        passwordResetTokenRepository.save(token);

        deactivateActiveSessions(user.getId());
        log.info("Password reset completed for {}", user.getEmail());
    }

    @SuppressWarnings("null")
    private void handlePasswordResetRequest(User user, Instant now) {
        markExistingTokensAsUsed(user.getId(), now);
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .token(generateSecureToken())
                .expiresAt(passwordResetProperties.calculateExpiryFrom(now))
                .build();
        PasswordResetToken savedToken = passwordResetTokenRepository.save(token);

        String resetLink = buildResetLink(savedToken.getToken());
        passwordResetNotificationService.sendPasswordResetEmail(user, resetLink, savedToken.getExpiresAt());
        log.info("Password reset instructions generated for {}", user.getEmail());
    }

    private void markExistingTokensAsUsed(UUID userId, Instant now) {
        List<PasswordResetToken> activeTokens = passwordResetTokenRepository.findAllByUserIdAndUsedFalse(userId);
        if (activeTokens.isEmpty()) {
            return;
        }
        activeTokens.forEach(token -> {
            token.setUsed(true);
            token.setUsedAt(now);
        });
        passwordResetTokenRepository.saveAll(activeTokens);
    }

    private String buildResetLink(String token) {
        String baseUrl = passwordResetProperties.getFrontendBaseUrl();
        String separator = baseUrl.contains("?") ? "&" : "?";
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        return baseUrl + separator + "token=" + encodedToken;
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private void deactivateActiveSessions(UUID userId) {
        List<UserSession> sessions = userSessionRepository.findAllByUserIdAndActiveTrue(userId);
        if (sessions.isEmpty()) {
            return;
        }
        Instant now = Instant.now();
        sessions.forEach(session -> {
            session.setActive(false);
            session.setLogoutTime(now);
        });
        userSessionRepository.saveAll(sessions);
    }
}
