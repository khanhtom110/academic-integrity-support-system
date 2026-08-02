package com.haui.lms.controller;

import com.haui.lms.base.ApiResponse;
import com.haui.lms.constant.ApiPath;
import com.haui.lms.constant.CommonConstant;
import com.haui.lms.constant.SuccessMessage;
import com.haui.lms.constant.UrlConstant;
import com.haui.lms.dto.*;
import com.haui.lms.dto.request.*;
import com.haui.lms.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping(ApiPath.API_V1)
@Tag(name = "Authentication")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Đăng ký tài khoản", description = "Dùng để đăng ký tài khoản")
    @PostMapping(UrlConstant.Auth.REGISTER)
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authenticationService.initRegister(request);
        return ResponseEntity.ok(ApiResponse.ok(SuccessMessage.Auth.SEND_OTP_SUCCESS, null));
    }

    @Operation(summary = "Xác thực OTP", description = "Dùng để kích hoạt tài khoản")
    @PostMapping(UrlConstant.Auth.VERIFY_OTP)
    public ResponseEntity<ApiResponse<RegisterResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        RegisterResponse response = authenticationService.verifyOtp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(SuccessMessage.Auth.REGISTER_SUCCESS, response));
    }

    // - POST /api/v1/auth/resend-otp (Em nhớ xem cái này có thể tái sử dụng lại cái gửi email của register hay k)
    @Operation(summary = "Gửi lại OTP", description = "Dùng để gửi lại OTP")
    @PostMapping(UrlConstant.Auth.RESEND_OTP)
    public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        authenticationService.resendOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("", null));
    }

    @Operation(summary = "Đăng nhập tài khoản", description = "Dùng để đăng nhập tài khoản")
    @PostMapping(UrlConstant.Auth.LOGIN)
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(SuccessMessage.Auth.LOGIN_SUCCESS, response));
    }

    @Operation(summary = "Cấp lại Access Token mới", description = "Sử dụng Refresh Token (còn hiệu lực) để cấp lại access token")
    @PostMapping(UrlConstant.Auth.REFRESH_TOKEN)
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.ok("", response));
    }

    @Operation(summary = "Đăng xuất hệ thống", description = "Vô hiệu hóa (invalidate) Refresh Token hiện tại, đưa token này vào danh sách đen")
    @PostMapping(UrlConstant.User.LOGOUT)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authenticationService.logout(request);
        return ResponseEntity.ok(ApiResponse.ok(SuccessMessage.Auth.LOGOUT_SUCCESS, null));
    }
}