package com.haui.lms.controller;

import com.haui.lms.base.ApiResponse;
import com.haui.lms.constant.ApiPath;
import com.haui.lms.constant.UrlConstant;
import com.haui.lms.dto.request.GoogleLoginRequest;
import com.haui.lms.dto.response.AuthResponse;
import com.haui.lms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.haui.lms.entity.User;
import com.haui.lms.repository.UserRepository;

@RestController
@RequestMapping(ApiPath.API_V1)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping(UrlConstant.Auth.GOOGLE)
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // --- HÀM LẤY TOKEN TEST TẠM THỜI ---
    @GetMapping(UrlConstant.Auth.GET_TOKEN_TEST)
    public ResponseEntity<ApiResponse<String>> getTestToken(@RequestParam String email) {
        String token = authService.generateTestToken(email);
        return ResponseEntity.ok(ApiResponse.ok(token));
    }
}