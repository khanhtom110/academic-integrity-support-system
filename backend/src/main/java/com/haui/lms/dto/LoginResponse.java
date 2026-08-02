package com.haui.lms.dto;

import com.haui.lms.constant.CommonConstant;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class LoginResponse {
    String accessToken;
    String refreshToken;
    UUID id;
    @Builder.Default
    String tokenType = CommonConstant.BEARER_TOKEN;
}
