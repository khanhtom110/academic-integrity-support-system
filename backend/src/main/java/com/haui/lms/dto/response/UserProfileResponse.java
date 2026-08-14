package com.haui.lms.dto.response;

import com.haui.lms.entity.Role;
import com.haui.lms.entity.User;

import java.util.UUID;

public record UserProfileResponse(UUID id, String email, String fullName, String phone, String address, String avatar,
        Role role, Boolean isActive) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getFullName(), user.getPhone(),
                user.getAddress(), user.getAvatar(), user.getRole(), user.getIsActive());
    }
}
