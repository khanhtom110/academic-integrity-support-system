package com.haui.lms.dto;

import com.haui.lms.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterResponse(@Schema(description = "Địa chỉ email đã đăng ký") String email) {
    public static RegisterResponse from(User user) {
        return new RegisterResponse(user.getEmail());
    }
}
