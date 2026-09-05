package com.haui.lms.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.haui.lms.constant.ErrorMessage;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @JsonProperty("refreshToken") @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD) String refreshToken) {
}
