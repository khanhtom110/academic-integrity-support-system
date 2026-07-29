package com.haui.lms.service.impl;

import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.exception.extended.AppException;
import com.haui.lms.mock.entity.User;
import com.haui.lms.mock.request.LoginRequestDto;
import com.haui.lms.mock.response.LoginResponseDto;
import com.haui.lms.mock.repository.UserRepository;
import com.haui.lms.mock.security.JwtProvider;
import com.haui.lms.service.AuthenticationService;
import com.haui.lms.service.UserService;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthenticationServiceImpl implements AuthenticationService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    JwtProvider jwtProvider;
    UserService userService;

    @NonFinal
    @Value("${jwt.access.expiration_time}")
    long ACCESS_TOKEN_EXPIRATION;

    @NonFinal
    @Value("${jwt.refresh.expiration_time}")
    long REFRESH_TOKEN_EXPIRATION;


    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        // 1. Tim kiem username trong csdl, neu ko thay thi tra ve 401
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(
                        401, ErrorMessage.Auth.INVALID_CREDENTIALS));
        // 2. Xac thuc mat khau, neu 0 xac thuc -> 401
        boolean auth = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!auth) {
            throw new AppException(401, ErrorMessage.Auth.INVALID_CREDENTIALS);
        }
        // 3. Tao token
        String accessToken = jwtProvider.generateToken(user, ACCESS_TOKEN_EXPIRATION);
        // 4. Tao response
    }
}
