package com.haui.lms.controller;

import com.haui.lms.base.ApiResponse;
import com.haui.lms.constant.SuccessMessage;
import com.haui.lms.constant.UrlConstant;
import com.haui.lms.dto.*;
import com.haui.lms.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication Controller")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Đăng ký tài khoản", description = "Dùng để đăng ký tài khoản")
    @PostMapping(UrlConstant.Auth.REGISTER)
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authenticationService.initRegister(request);
        return ResponseEntity.ok(ApiResponse.ok("", null));
    }

    @Operation(summary = "Xác thực OTP", description = "Dùng để kích hoạt tài khoản")
    @PostMapping(UrlConstant.Auth.VERIFY_OTP)
    public ResponseEntity<ApiResponse<RegisterResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        RegisterResponse response = authenticationService.verifyOtp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(SuccessMessage.Auth.REGISTER_SUCCESS, response));
    }
    // - POST /api/v1/auth/resend-otp (Em nhớ xem cái này có thể tái sử dụng lại cái gửi email của register hay k)
    // @Operation(summary = "Gửi lại OTP", description = "Dùng để gửi lại OTP")
    // @PostMapping(UrlConstant.Auth.RESEND_OTP)
    // public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody)

    @Operation(summary = "Đăng nhập tài khoản", description = "Dùng để đăng nhập tài khoản")
    @PostMapping(UrlConstant.Auth.LOGIN)
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(SuccessMessage.Auth.LOGIN_SUCCESS, response));
    }
    // - POST /api/v1/auth/refresh-token (Cấp Access Token mới từ Refresh Token)
    // - POST /api/v1/auth/logout
}
