package com.bookfair.auth_service.repository;

import com.bookfair.auth_service.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByRefreshToken(String refreshToken);

    Optional<UserSession> findByAccessToken(String accessToken);

    Optional<UserSession> findByRefreshTokenAndActiveTrue(String refreshToken);

    Optional<UserSession> findByAccessTokenAndActiveTrue(String accessToken);
}
