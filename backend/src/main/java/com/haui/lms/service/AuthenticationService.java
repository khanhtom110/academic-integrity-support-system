package com.haui.lms.service;

import com.haui.lms.dto.*;
import com.haui.lms.dto.request.LoginRequest;
import com.haui.lms.dto.request.LogoutRequest;
import com.haui.lms.dto.request.RegisterRequest;
import com.haui.lms.dto.request.VerifyOtpRequest;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request);

    void initRegister(RegisterRequest request);

    RegisterResponse verifyOtp(VerifyOtpRequest request);

    void logout(LogoutRequest request);
}
