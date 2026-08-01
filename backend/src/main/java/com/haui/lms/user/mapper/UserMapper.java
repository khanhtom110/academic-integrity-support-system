package com.haui.lms.user.mapper;

import com.haui.lms.dto.UserProfileResponse;
import com.haui.lms.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getFullName(), user.getPhone(),
                user.getAddress(), user.getRole(), user.getIsActive());
    }
}
