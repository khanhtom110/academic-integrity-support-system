package com.haui.lms.dto.request;

import com.haui.lms.constant.CommonConstant;
import com.haui.lms.constant.ErrorMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record ChangePasswordRequest(
        @Schema(description = "Mật khẩu hiện tại") @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD) String oldPassword,

        @Schema(description = "Mật khẩu mới") @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD) @Size(min = CommonConstant.PASSWORD_MIN_LENGTH, max = CommonConstant.PASSWORD_LENGTH, message = ErrorMessage.INVALID_FORMAT_PASSWORD) @Pattern(regexp = CommonConstant.PASSWORD_REGEX, message = ErrorMessage.INVALID_FORMAT_PASSWORD) String newPassword,

        @Schema(description = "Xác nhận mật khẩu mới") @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD) String confirmPassword) {
}