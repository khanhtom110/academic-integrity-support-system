package com.haui.lms.dto.request;

import com.haui.lms.constant.ErrorMessage;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(@NotBlank(message = ErrorMessage.NOT_BLANK_FIELD) String refreshToken) {
}
