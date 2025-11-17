package com.bookfair.auth_service.dto;

import com.bookfair.auth_service.entity.UserRole;
import com.bookfair.auth_service.entity.UserStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class UserResponse {
    UUID id;
    String firstName;
    String lastName;
    String companyName;
    String email;
    String mobileNo;
    UserRole role;
    UserStatus status;
    Instant createdAt;
    Instant updatedAt;
}

