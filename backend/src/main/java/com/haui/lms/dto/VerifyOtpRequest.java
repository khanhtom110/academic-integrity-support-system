package com.haui.lms.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import com.haui.lms.constant.ErrorMessage;

@Getter
@Setter
public class VerifyOtpRequest {
    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    @Email(message = ErrorMessage.INVALID_FORMAT_EMAIL)
    private String email;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    @Pattern(regexp = "\\d{6}", message = ErrorMessage.Auth.INVALID_FORMAT_OTP)
    private String otp;
}
