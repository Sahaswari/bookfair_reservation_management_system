package com.bookfair.auth_service.controller;

import com.bookfair.auth_service.dto.ApiResponse;
import com.bookfair.auth_service.dto.AuthResponse;
import com.bookfair.auth_service.dto.ForgotPasswordRequest;
import com.bookfair.auth_service.dto.LoginRequest;
import com.bookfair.auth_service.dto.RefreshTokenRequest;
import com.bookfair.auth_service.dto.RegistrationRequest;
import com.bookfair.auth_service.dto.ResetPasswordRequest;
import com.bookfair.auth_service.service.UserService;
import com.bookfair.auth_service.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody @Valid RegistrationRequest request, HttpServletRequest servletRequest) {
        String deviceInfo = servletRequest.getHeader(HttpHeaders.USER_AGENT);
        String ipAddress = servletRequest.getRemoteAddr();
        AuthResponse response = userService.registerUser(request, deviceInfo, ipAddress);
        ApiResponse<AuthResponse> apiResponse = new ApiResponse<>(true, "User registered successfully", response, Instant.now());
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody @Valid LoginRequest request, HttpServletRequest servletRequest) {
        String deviceInfo = servletRequest.getHeader(HttpHeaders.USER_AGENT);
        String ipAddress = servletRequest.getRemoteAddr();
        AuthResponse response = userService.login(request, deviceInfo, ipAddress);
        ApiResponse<AuthResponse> apiResponse = new ApiResponse<>(true, "Login successful", response, Instant.now());
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        AuthResponse response = userService.refreshToken(request);
        ApiResponse<AuthResponse> apiResponse = new ApiResponse<>(true, "Token refreshed successfully", response, Instant.now());
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            userService.logout(authHeader.substring(7));
        }
        ApiResponse<Void> apiResponse = new ApiResponse<>(true, "Logout successful", null, Instant.now());
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        passwordResetService.sendResetInstructions(request);
        return ResponseEntity.ok(ApiResponse.success("If the email exists, reset instructions have been sent", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }
}
