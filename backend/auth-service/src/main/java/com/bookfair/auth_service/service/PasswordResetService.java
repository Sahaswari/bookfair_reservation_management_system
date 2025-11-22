package com.bookfair.auth_service.service;

import com.bookfair.auth_service.dto.ForgotPasswordRequest;
import com.bookfair.auth_service.dto.ResetPasswordRequest;

public interface PasswordResetService {

    void sendResetInstructions(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
