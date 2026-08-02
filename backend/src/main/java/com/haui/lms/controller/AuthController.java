package com.haui.lms.controller;

import com.haui.lms.base.ApiResponse;
import com.haui.lms.constant.ApiPath;
import com.haui.lms.constant.UrlConstant;
import com.haui.lms.dto.request.FacebookLoginRequest;
import com.haui.lms.dto.request.GoogleLoginRequest;
import com.haui.lms.dto.request.OutlookLoginRequest;
import com.haui.lms.dto.response.AuthResponse;
import com.haui.lms.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPath.API_V1)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Các API liên quan đến xác thực người dùng (Login, OAuth2)")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Đăng nhập bằng Google OAuth2", description = "Nhận authorization code từ Google Client, đăng ký/liên kết tài khoản")
    @PostMapping(UrlConstant.Auth.GOOGLE)
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Đăng nhập bằng Facebook OAuth2", description = "Nhận authorization code từ Facebook Client. Yêu cầu tài khoản Facebook phải có Email.")
    @PostMapping(UrlConstant.Auth.FACEBOOK)
    public ResponseEntity<ApiResponse<AuthResponse>> facebookLogin(@Valid @RequestBody FacebookLoginRequest request) {
        AuthResponse response = authService.loginWithFacebook(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Đăng nhập bằng Microsoft/Outlook OAuth2", description = "Nhận authorization code từ Microsoft Client")
    @PostMapping(UrlConstant.Auth.OUTLOOK)
    public ResponseEntity<ApiResponse<AuthResponse>> outlookLogin(@Valid @RequestBody OutlookLoginRequest request) {
        AuthResponse response = authService.loginWithOutlook(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}