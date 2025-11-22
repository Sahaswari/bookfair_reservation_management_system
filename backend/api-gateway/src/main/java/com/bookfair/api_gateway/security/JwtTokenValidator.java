package com.bookfair.api_gateway.security;

import com.bookfair.api_gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.crypto.SecretKey;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtTokenValidator {

    private final SecretKey secretKey;

    public JwtTokenValidator(JwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public Optional<TokenDetails> validate(String token) {
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            Claims claims = jws.getBody();
            String subject = claims.getSubject();
            if (!StringUtils.hasText(subject)) {
                return Optional.empty();
            }
            return Optional.of(subject)
                    .map(sub -> new TokenDetails(sub, claims.get("role", String.class)));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    @Getter
    public static class TokenDetails {
        private final String userId;
        private final String role;

        public TokenDetails(String userId, String role) {
            this.userId = userId;
            this.role = role;
        }
    }
}
