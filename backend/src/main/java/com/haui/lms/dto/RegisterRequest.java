package com.haui.lms.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import com.haui.lms.constant.CommonConstant;
import com.haui.lms.constant.ErrorMessage;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    @Size(min = CommonConstant.FULLNAME_MIN_LENGTH, max = CommonConstant.FULLNAME_LENGTH, message = ErrorMessage.INVALID_FORMAT_FULLNAME)
    private String fullName;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    @Email(message = ErrorMessage.INVALID_FORMAT_EMAIL)
    @Size(max = CommonConstant.EMAIL_LENGTH, message = ErrorMessage.INVALID_FORMAT_EMAIL)
    private String email;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    @Size(min = CommonConstant.PASSWORD_MIN_LENGTH, max = CommonConstant.PASSWORD_LENGTH, message = ErrorMessage.INVALID_FORMAT_PASSWORD)
    private String password;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String confirmPassword;
}
