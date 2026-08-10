package com.haui.lms.service;

import com.haui.lms.dto.*;
import com.haui.lms.dto.request.*;
import jakarta.transaction.Transactional;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request);

    void initRegister(RegisterRequest request);

    RegisterResponse verifyOtp(VerifyOtpRequest request);

    void logout(LogoutRequest request);

    LoginResponse refreshToken(RefreshTokenRequest request);

    void resendOtp(ResendOtpRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void verifyResetOtp(VerifyOtpRequest request);

    void resetPassword(ResetPasswordRequest request);
}
