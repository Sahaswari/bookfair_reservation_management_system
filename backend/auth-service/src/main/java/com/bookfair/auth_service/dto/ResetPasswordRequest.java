package com.bookfair.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required")
    private String token;

    @NotBlank(message = "New password is mandatory")
    @Size(min = 8, max = 255, message = "Password must be between 8 to 255 characters")
    private String newPassword;
}
