package com.haui.lms.dto.request;

import com.haui.lms.constant.ErrorMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendOtpRequest(
        @Schema(description = "Email người dùng đã đăng ký") @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
        // @Email(message = ErrorMessage.INVALID_FORMAT_EMAIL)
        String email) {
}
