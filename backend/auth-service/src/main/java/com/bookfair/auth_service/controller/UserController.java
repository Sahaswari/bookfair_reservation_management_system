package com.bookfair.auth_service.controller;

import com.bookfair.auth_service.dto.ApiResponse;
import com.bookfair.auth_service.dto.UserResponse;
import com.bookfair.auth_service.security.CustomUserDetails;
import com.bookfair.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal CustomUserDetails currentUser) {
        UserResponse userResponse = userService.getProfile(currentUser.getUserId());
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>(true, "User profile retrieved successfully", userResponse, Instant.now());
        return ResponseEntity.ok(apiResponse);
    }
}
