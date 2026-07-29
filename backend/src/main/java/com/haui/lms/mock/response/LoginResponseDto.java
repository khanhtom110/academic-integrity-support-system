package com.haui.lms.mock.response;

import com.haui.lms.constant.CommonConstant;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level= AccessLevel.PRIVATE)
@Builder
public class LoginResponseDto {
    HttpStatus status;
    String message;
    String accessToken;
    String refreshToken;
    String id;
    @Builder.Default
    String tokenType = CommonConstant.BEARER_TOKEN;
}
