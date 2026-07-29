package com.haui.lms.service;

import com.haui.lms.mock.request.LoginRequestDto;
import com.haui.lms.mock.response.LoginResponseDto;

public interface AuthenticationService {
    LoginResponseDto login(LoginRequestDto request);
}
