package com.haui.lms.user.service;

import com.haui.lms.dto.UserProfileResponse;
import com.haui.lms.user.dto.request.ChangePasswordRequest;
import com.haui.lms.user.dto.request.ResetPasswordRequest;
import com.haui.lms.user.dto.request.UpdateProfileRequest;

import java.util.UUID;

public interface UserService {

    UserProfileResponse getProfile(UUID userId);

    UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);

    void changePassword(UUID userId, ChangePasswordRequest request);

    void requestPasswordReset(String email);

    void resetPassword(ResetPasswordRequest request);
}
