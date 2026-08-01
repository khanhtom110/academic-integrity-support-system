package com.haui.lms.user.service;

import com.haui.lms.dto.response.UserProfileResponse;
import com.haui.lms.user.dto.request.ChangePasswordRequest;
import com.haui.lms.user.dto.request.ResetPasswordRequest;
import com.haui.lms.user.dto.request.UpdateProfileRequest;

public interface UserService {

    UserProfileResponse getProfile(String email);

    UserProfileResponse updateProfile(String email, UpdateProfileRequest request);

    void changePassword(String email, ChangePasswordRequest request);

    void requestPasswordReset(String email);

    void resetPassword(ResetPasswordRequest request);
}