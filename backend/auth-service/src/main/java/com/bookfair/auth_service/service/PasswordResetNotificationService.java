package com.bookfair.auth_service.service;

import com.bookfair.auth_service.entity.User;

import java.time.Instant;

public interface PasswordResetNotificationService {

    void sendPasswordResetEmail(User user, String resetLink, Instant expiresAt);
}
