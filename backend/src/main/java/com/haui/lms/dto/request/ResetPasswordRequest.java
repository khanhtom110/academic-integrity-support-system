package com.haui.lms.dto.request;

import com.haui.lms.constant.CommonConstant;
import com.haui.lms.constant.ErrorMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        // @formatter:off
        @Schema(description = "Email tài khoản cần đặt lại mật khẩu") @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD) @Email(message = ErrorMessage.INVALID_FORMAT_EMAIL) String email,

        @Schema(description = "Mật khẩu mới") @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD) @Size(min = CommonConstant.PASSWORD_MIN_LENGTH, max = CommonConstant.PASSWORD_LENGTH, message = ErrorMessage.INVALID_FORMAT_PASSWORD) @Pattern(regexp = CommonConstant.PASSWORD_REGEX, message = ErrorMessage.INVALID_FORMAT_PASSWORD) String newPassword,

        @Schema(description = "Xác nhận mật khẩu mới") @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD) String confirmPassword) {
}
