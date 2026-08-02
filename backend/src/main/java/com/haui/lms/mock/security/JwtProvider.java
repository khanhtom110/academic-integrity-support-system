package com.haui.lms.mock.security;

import com.haui.lms.entity.User;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {
    public String generateToken(User user, long expirationTime) {
        return "abcabcabcabcabcabcabcabc";
    }
}
