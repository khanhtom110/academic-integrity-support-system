package com.haui.lms.mock;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level= AccessLevel.PRIVATE)
public class UserResponseDto {
    String id;
    String username;
    String email;
    String firstName;
    String lastName;
    Role role;
}
