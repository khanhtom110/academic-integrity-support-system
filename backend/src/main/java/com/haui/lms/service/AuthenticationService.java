package com.haui.lms.service;

import com.haui.lms.dto.*;
import com.haui.lms.dto.request.*;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request);

    void initRegister(RegisterRequest request);

    RegisterResponse verifyOtp(VerifyOtpRequest request);

    void logout(LogoutRequest request);

    LoginResponse refreshToken(RefreshTokenRequest request);
}
