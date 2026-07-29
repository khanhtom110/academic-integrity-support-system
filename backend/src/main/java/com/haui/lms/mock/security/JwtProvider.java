package com.haui.lms.mock.security;

import com.haui.lms.mock.entity.User;

public class JwtProvider {
    public String generateToken(User user, long expirationTime){
        return "abcabcabcabcabcabcabcabc";
    }
}
