package com.haui.lms.dto.response;

import com.haui.lms.entity.Role;
import java.util.UUID;

public record UserProfileResponse(UUID id, String email, String fullName, String phone, String address, Role role,
        Boolean isActive) {
}
