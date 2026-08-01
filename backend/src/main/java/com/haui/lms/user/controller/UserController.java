package com.haui.lms.user.controller;

import com.haui.lms.base.ApiResponse;
import com.haui.lms.constant.ApiPath;
import com.haui.lms.constant.SuccessMessage;
import com.haui.lms.constant.UrlConstant;
import com.haui.lms.dto.response.UserProfileResponse;
import com.haui.lms.user.dto.request.ChangePasswordRequest;
import com.haui.lms.user.dto.request.UpdateProfileRequest;
import com.haui.lms.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPath.API_V1)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping(UrlConstant.User.GET_PROFILE)
    public ApiResponse<UserProfileResponse> getProfile() {
        return ApiResponse.ok(userService.getProfile(currentEmail()));
    }

    @PostMapping(UrlConstant.User.GET_PROFILE)
    public ApiResponse<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
        return ApiResponse.ok(SuccessMessage.User.UPDATE_PROFILE_SUCCESS,
                userService.updateProfile(currentEmail(), updateProfileRequest));
    }

    @PostMapping(UrlConstant.User.CHANGE_PASSWORD)
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(currentEmail(), changePasswordRequest);
        return ApiResponse.ok(SuccessMessage.User.CHANGE_PASSWORD_SUCCESS, null);
    }

    private String currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}