package com.haui.lms.mock.request;

import com.haui.lms.constant.ErrorMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level= AccessLevel.PRIVATE)
public class LoginRequestDto {
    @Schema(description = "Tên đăng nhập", example = "user123")
    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    String username;

    @Schema(description = "Tên mật khẩu", example = "User123")
    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    String password;
}
