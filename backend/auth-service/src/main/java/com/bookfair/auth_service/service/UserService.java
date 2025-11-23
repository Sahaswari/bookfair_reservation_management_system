package com.bookfair.auth_service.service;

import com.bookfair.auth_service.dto.AuthResponse;
import com.bookfair.auth_service.dto.LoginRequest;
import com.bookfair.auth_service.dto.RefreshTokenRequest;
import com.bookfair.auth_service.dto.RegistrationRequest;
import com.bookfair.auth_service.dto.UpdateUserRequest;
import com.bookfair.auth_service.dto.UserResponse;

import java.util.UUID;

public interface UserService {

    AuthResponse registerUser(RegistrationRequest request, String deviceInfo, String ipAddress);

    AuthResponse login(LoginRequest request, String deviceInfo, String ipAddress);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String accessToken);

    UserResponse getProfile(UUID userId);

    UserResponse updateUser(UUID userId, UpdateUserRequest request);

    void deleteUser(UUID userId);
}
