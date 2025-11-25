package com.bookfair.auth_service.repository;

import com.bookfair.auth_service.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByToken(String token);

    List<PasswordResetToken> findAllByUserIdAndUsedFalse(UUID userId);

    void deleteByExpiresAtBefore(Instant cutoff);
}
