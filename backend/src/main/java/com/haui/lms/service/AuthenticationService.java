package com.haui.lms.service;

import com.haui.lms.dto.*;
import org.springframework.stereotype.Service;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request);

    void initRegister(RegisterRequest request);

    RegisterResponse verifyOtp(VerifyOtpRequest request);
}
