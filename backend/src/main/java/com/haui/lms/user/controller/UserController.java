package com.haui.lms.user.controller;

import com.haui.lms.base.ApiResponse;
import com.haui.lms.constant.ApiPath;
import com.haui.lms.constant.SuccessMessage;
import com.haui.lms.constant.UrlConstant;
import com.haui.lms.dto.UserProfileResponse;
import com.haui.lms.user.dto.request.ChangePasswordRequest;
import com.haui.lms.user.dto.request.UpdateProfileRequest;
import com.haui.lms.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiPath.API_V1)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping(UrlConstant.User.GET_PROFILE)
    public ApiResponse<UserProfileResponse> getProfile(HttpServletRequest request) {
        UUID currentUserId = CurrentUserHolder.getCurrentUserId(request);
        return ApiResponse.ok(userService.getProfile(currentUserId));
    }

    @PostMapping(UrlConstant.User.GET_PROFILE)
    public ApiResponse<UserProfileResponse> updateProfile(HttpServletRequest request,
            @Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
        UUID currentUserId = CurrentUserHolder.getCurrentUserId(request);
        return ApiResponse.ok(SuccessMessage.User.UPDATE_PROFILE_SUCCESS,
                userService.updateProfile(currentUserId, updateProfileRequest));
    }

    @PostMapping(UrlConstant.User.CHANGE_PASSWORD)
    public ApiResponse<Void> changePassword(HttpServletRequest request,
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        UUID currentUserId = CurrentUserHolder.getCurrentUserId(request);
        userService.changePassword(currentUserId, changePasswordRequest);
        return ApiResponse.ok(SuccessMessage.User.CHANGE_PASSWORD_SUCCESS, null);
    }
}
