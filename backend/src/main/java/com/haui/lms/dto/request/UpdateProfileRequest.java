package com.haui.lms.dto.request;

import com.haui.lms.constant.CommonConstant;
import com.haui.lms.constant.ErrorMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Email khong nam trong request: email la dinh danh tai khoan nen khong cho doi
public record UpdateProfileRequest(
        @Schema(description = "Họ và tên hiển thị") @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD) @Size(min = CommonConstant.FULLNAME_MIN_LENGTH, max = CommonConstant.FULLNAME_LENGTH, message = ErrorMessage.INVALID_FORMAT_FULLNAME) String fullName) {
}
