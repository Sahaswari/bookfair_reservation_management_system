package com.bookfair.auth_service.service.impl;

import com.bookfair.auth_service.dto.AuthResponse;
import com.bookfair.auth_service.dto.LoginRequest;
import com.bookfair.auth_service.dto.RefreshTokenRequest;
import com.bookfair.auth_service.dto.RegistrationRequest;
import com.bookfair.auth_service.dto.TokenResponse;
import com.bookfair.auth_service.dto.UserResponse;
import com.bookfair.auth_service.entity.User;
import com.bookfair.auth_service.entity.UserRole;
import com.bookfair.auth_service.entity.UserSession;
import com.bookfair.auth_service.entity.UserStatus;
import com.bookfair.auth_service.exception.DuplicateResourceException;
import com.bookfair.auth_service.exception.InvalidCredentialsException;
import com.bookfair.auth_service.mapper.UserMapper;
import com.bookfair.auth_service.repository.UserRepository;
import com.bookfair.auth_service.repository.UserSessionRepository;
import com.bookfair.auth_service.security.JwtTokenProvider;
import com.bookfair.auth_service.service.UserService;
import com.bookfair.auth_service.messaging.UserEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final UserEventPublisher userEventPublisher;

    @Override
    @Transactional
    public AuthResponse registerUser(RegistrationRequest request, String deviceInfo, String ipAddress) {
        log.info("Registering user with email {}", request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (userRepository.existsByMobileNo(request.getMobileNo())) {
            throw new DuplicateResourceException("Mobile number already registered");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .companyName(request.getCompanyName())
                .email(request.getEmail().toLowerCase())
                .mobileNo(request.getMobileNo())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() == null ? UserRole.VENDOR : request.getRole())
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(user);
        publishUserRegistered(user);
        return buildAuthResponse(user, deviceInfo, ipAddress);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String deviceInfo, String ipAddress) {
        log.info("Logging in user with email {}", request.getEmail());
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        return buildAuthResponse(user, deviceInfo, ipAddress);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        UserSession session = userSessionRepository.findByRefreshTokenAndActiveTrue(request.getRefreshToken())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new InvalidCredentialsException("Refresh token expired or invalid");
        }

        User user = session.getUser();
        deactivateSession(session);
        return buildAuthResponse(user, session.getDeviceInfo(), session.getIpAddress());
    }

    @Override
    @Transactional
    public void logout(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }
        userSessionRepository.findByAccessTokenAndActiveTrue(accessToken.trim()).ifPresent(this::deactivateSession);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        return userMapper.toResponse(loadUserById(userId));
    }

    public User loadUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
    }

    private AuthResponse buildAuthResponse(User user, String deviceInfo, String ipAddress) {
        String resolvedDevice = (deviceInfo == null || deviceInfo.isBlank()) ? "unknown" : deviceInfo;
        String resolvedIp = (ipAddress == null || ipAddress.isBlank()) ? "unknown" : ipAddress;
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId().toString(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().toString());
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtTokenProvider.getAccessTokenValidity());

        UserSession session = UserSession.builder()
                .user(user)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .deviceInfo(resolvedDevice)
                .ipAddress(resolvedIp)
                .loginTime(now)
                .expiresAt(expiry)
                .active(true)
                .build();
        userSessionRepository.save(session);

        return AuthResponse.builder()
                .user(userMapper.toResponse(user))
                .tokens(TokenResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .expiresIn(jwtTokenProvider.getAccessTokenValidity())
                        .build())
                .build();
    }

    private void deactivateSession(UserSession session) {
        session.setActive(false);
        session.setLogoutTime(Instant.now());
        userSessionRepository.save(session);
    }

    private void publishUserRegistered(User user) {
        try {
            userEventPublisher.publishUserRegistered(user);
        } catch (Exception ex) {
            log.warn("Failed to publish user registered event for {}: {}", user.getEmail(), ex.getMessage());
        }
    }
}
