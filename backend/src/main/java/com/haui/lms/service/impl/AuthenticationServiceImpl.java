package com.haui.lms.service.impl;

import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.dto.*;
import com.haui.lms.entity.Role;
import com.haui.lms.entity.User;
import com.haui.lms.exception.extended.AppException;
import com.haui.lms.mock.security.JwtProvider;
import com.haui.lms.repository.UserRepository;
import com.haui.lms.service.AuthenticationService;
import com.haui.lms.service.MailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final MailService mailService;

    @NonFinal
    @Value("${jwt.access.expiration_time}")
    long ACCESS_TOKEN_EXPIRATION;

    @NonFinal
    @Value("${jwt.refresh.expiration_time}")
    long REFRESH_TOKEN_EXPIRATION;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(401, ErrorMessage.Auth.INVALID_CREDENTIALS));

        boolean auth = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!auth) {
            throw new AppException(401, ErrorMessage.Auth.INVALID_CREDENTIALS);
        }
        String accessToken = jwtProvider.generateToken(user, ACCESS_TOKEN_EXPIRATION);
        String refreshToken = jwtProvider.generateToken(user, REFRESH_TOKEN_EXPIRATION);

        return LoginResponse.builder().accessToken(accessToken).refreshToken(refreshToken).id(user.getId()).build();
    }

    @Override
    @Transactional
    public void initRegister(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(400, ErrorMessage.PASSWORD_MISMATCH);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(400, ErrorMessage.User.EMAIL_EXISTED);
        }

        String otp = String.format("%06d", new Random().nextInt(1000000));

        // Luu otp
        try {
            String userInfo = objectMapper.writeValueAsString(request);
            redisTemplate.opsForValue().set("REGISTRATION_DATA:" + request.getEmail(), userInfo, 5, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set("REGISTRATION_OTP:" + request.getEmail(), otp, 5, TimeUnit.MINUTES);
            System.out.println("DEBUG - Đã set OTP vào Redis với key: REGISTRATION_OTP:" + request.getEmail()
                    + " và giá trị: " + otp);
        } catch (JacksonException e) {
            throw new AppException(500, ErrorMessage.EXCEPTION_GENERAL);
        }

        // Gui email
        mailService.sendOtp(request.getEmail(), otp);
    }

    @Override
    @Transactional
    public RegisterResponse verifyOtp(VerifyOtpRequest request) {
        String registerOtp = redisTemplate.opsForValue().get("REGISTRATION_OTP:" + request.getEmail());
        if (registerOtp == null || !registerOtp.equals(request.getOtp())) {
            System.out.println("Register OTP in Redis: " + registerOtp);
            throw new AppException(400, ErrorMessage.Auth.INVALID_OTP);
        }

        String userInfo = redisTemplate.opsForValue().get("REGISTRATION_DATA:" + request.getEmail());
        if (userInfo == null) {
            throw new AppException(400, ErrorMessage.Auth.SESSION_EXPIRED);
        }

        RegisterRequest registerRequest = objectMapper.readValue(userInfo, RegisterRequest.class);
        String password = passwordEncoder.encode(registerRequest.getPassword());
        User user = User.builder().email(registerRequest.getEmail()).password(password)
                .fullName(registerRequest.getFullName()).role(Role.USER).isActive(true).build();

        redisTemplate.delete("REGISTER_OTP:" + request.getEmail());
        redisTemplate.delete("REGISTER_DATA:" + request.getEmail());

        User savedUser = userRepository.save(user);
        return RegisterResponse.from(savedUser);
    }
}
