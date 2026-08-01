package com.haui.lms.user.dto.request;

import com.haui.lms.constant.ErrorMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(

        @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD) @Email(message = ErrorMessage.INVALID_FORMAT_EMAIL) String email) {
}
