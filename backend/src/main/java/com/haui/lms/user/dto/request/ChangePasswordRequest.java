package com.haui.lms.user.dto.request;

import com.haui.lms.constant.ErrorMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(

        @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD) String oldPassword,

        @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD) @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,120}$", message = ErrorMessage.INVALID_FORMAT_PASSWORD) String newPassword,

        @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD) String confirmPassword) {
}
