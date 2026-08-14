package com.haui.lms.service;

import com.haui.lms.dto.request.UpdateProfileRequest;
import com.haui.lms.dto.response.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Cac ham deu nhan email lay tu JWT (khong phai tu client gui len) nen user chi thao tac duoc tren ho so cua chinh
 * minh.
 */
public interface UserService {

    UserProfileResponse getProfile(String email);

    UserProfileResponse updateProfile(String email, UpdateProfileRequest request);

    UserProfileResponse updateAvatar(String email, MultipartFile file);

    UserProfileResponse deleteAvatar(String email);
}
