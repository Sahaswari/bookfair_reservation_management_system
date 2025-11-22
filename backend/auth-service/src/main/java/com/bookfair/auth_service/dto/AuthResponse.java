package com.bookfair.auth_service.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    UserResponse user;
    TokenResponse tokens;
}

