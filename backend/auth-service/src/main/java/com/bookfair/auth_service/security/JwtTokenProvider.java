package com.bookfair.auth_service.security;

import com.bookfair.auth_service.config.JwtProperties;
import com.bookfair.auth_service.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String generateAccessToken(String subject, UserRole role) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.getAccessTokenValidity());
        return buildToken(subject, role, now, expiry, Map.of("type", "ACCESS"));
    }

    public String generateRefreshToken(String subject) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.getRefreshTokenValidity());
        return buildToken(subject, null, now, expiry, Map.of("type", "REFRESH"));
    }

    public long getAccessTokenValidity() {
        return jwtProperties.getAccessTokenValidity();
    }

    public long getRefreshTokenValidity() {
        return jwtProperties.getRefreshTokenValidity();
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    private String buildToken(String subject, UserRole role, Instant issuedAt, Instant expiry, Map<String, Object> additionalClaims) {
        Claims claims = Jwts.claims()
                .setSubject(subject)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiry));
        if (role != null) {
            claims.put("role", role.name());
        }
        if (additionalClaims != null) {
            claims.putAll(additionalClaims);
        }
        return Jwts.builder()
                .setClaims(claims)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }
}
