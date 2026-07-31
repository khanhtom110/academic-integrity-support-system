package com.haui.lms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import static com.haui.lms.constant.ErrorMessage.INVALID_FORMAT_EMAIL;
import static com.haui.lms.constant.ErrorMessage.NOT_BLANK_FIELD;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = NOT_BLANK_FIELD)
    @Email(message = INVALID_FORMAT_EMAIL)
    private String email;

    @NotBlank(message = NOT_BLANK_FIELD)
    private String password;
}
