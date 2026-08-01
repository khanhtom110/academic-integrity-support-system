package com.haui.lms.user.controller;

import com.haui.lms.base.ApiResponse;
import com.haui.lms.constant.ApiPath;
import com.haui.lms.constant.SuccessMessage;
import com.haui.lms.constant.UrlConstant;
import com.haui.lms.user.dto.request.ForgotPasswordRequest;
import com.haui.lms.user.dto.request.ResetPasswordRequest;
import com.haui.lms.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Flow Quen mat khau - dung 2 endpoint theo dung phan cong lead da chot: POST /auth/forgot-password/request -> gui OTP
 * POST /auth/forgot-password/confirm -> verify OTP + doi mat khau (1 buoc)
 */
@RestController
@RequestMapping(ApiPath.API_V1)
@RequiredArgsConstructor
public class PasswordController {

    private final UserService userService;

    @PostMapping(UrlConstant.Auth.FORGOT_PASSWORD_REQUEST)
    public ApiResponse<Void> requestReset(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.requestPasswordReset(request.email());
        return ApiResponse.ok(SuccessMessage.Auth.SEND_OTP_SUCCESS, null);
    }

    @PostMapping(UrlConstant.Auth.FORGOT_PASSWORD_CONFIRM)
    public ApiResponse<Void> confirmResetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ApiResponse.ok(SuccessMessage.Auth.RESET_PASSWORD_SUCCESS, null);
    }
}
