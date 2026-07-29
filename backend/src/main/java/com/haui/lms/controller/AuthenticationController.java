package com.haui.lms.controller;

import com.haui.lms.base.ApiResponse;
import com.haui.lms.constant.UrlConstant;
import com.haui.lms.mock.request.LoginRequestDto;
import com.haui.lms.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // POST /api/v1/auth/register: Đăng ký tài khoản (tạo user, gen OTP 6 số, lưu Redis, gửi qua Email).
    @Operation(summary = "Đăng ký tài khoản", description = "Dùng để đăng ký tài khoản")
    @PostMapping(UrlConstant.Auth.REGISTER)
    public ResponseEntity<ApiResponse<>> register(){
        return "ApiResponse.ok(respone);";
    }

    // POST /api/v1/auth/verify-otp: Xác thực OTP từ Redis để activate tài khoản.
    // POST /api/v1/auth/resend-otp: Gửi lại mã OTP qua email, update TTL trong Redis.
    // POST /api/v1/auth/login: Xác thực user/pass, cấp Access Token và Refresh Token (lưu Refresh Token vào Redis TTL 7 ngày).
    @Operation(summary = "Đăng nhập tài khoản", description = "Dùng để đăng nhập tài khoản")
    @PostMapping(UrlConstant.Auth.LOGIN)
    public ResponseEntity<ApiResponse<>> login(@Valid @RequestBody LoginRequestDto requestDTO){
        LoginRequestDto response = authenticationService.login(requestDTO);
        return ApiResponse.ok(response);
    }
    // POST /api/v1/auth/refresh-token: Nhận Refresh Token cũ, kiểm tra tồn tại trong Redis, nếu hợp lệ thì issue bộ Token mới.
    // POST /api/v1/auth/logout: Xóa RT:<userId> khỏi Redis.


}
