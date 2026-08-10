package com.haui.lms.service.impl;

import com.haui.lms.constant.CommonConstant;
import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.dto.*;
import com.haui.lms.dto.request.*;
import com.haui.lms.entity.Role;
import com.haui.lms.entity.User;
import com.haui.lms.exception.extended.AppException;
import com.haui.lms.repository.InvalidatedRepository;
import com.haui.lms.repository.UserRepository;
import com.haui.lms.security.CustomUserDetails;
import com.haui.lms.security.JwtService;
import com.haui.lms.service.AuthenticationService;
import com.haui.lms.service.MailService;
import com.nimbusds.jwt.SignedJWT;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.text.ParseException;
import java.time.Instant;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final MailService mailService;
    private final InvalidatedRepository invalidatedRepository;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(401, ErrorMessage.Auth.INVALID_CREDENTIALS));

        boolean auth = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!auth) {
            throw new AppException(401, ErrorMessage.Auth.INVALID_CREDENTIALS);
        }
        String accessToken = jwtService.generateToken(user, false);
        String refreshToken = jwtService.generateToken(user, true);

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

        redisTemplate.delete("REGISTRATION_OTP:" + request.getEmail());
        redisTemplate.delete("REGISTRATION_DATA:" + request.getEmail());

        User savedUser = userRepository.save(user);
        return RegisterResponse.from(savedUser);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        try {
            String token = request.refreshToken();
            if (jwtService.isAccessToken(token)) {
                throw new AppException(400, ErrorMessage.Auth.INVALID_LOGOUT_TOKEN);
            }

            SignedJWT signedJWT = SignedJWT.parse(token);
            String jti = signedJWT.getJWTClaimsSet().getJWTID();

            if (invalidatedRepository.existsById(jti)) {
                throw new AppException(400, ErrorMessage.Auth.TOKEN_ALREADY_INVALIDATED);
            }

            jwtService.invalidateToken(token);

        } catch (ParseException e) {
            throw new AppException(400, ErrorMessage.Auth.MALFORMED_TOKEN);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(500, ErrorMessage.EXCEPTION_GENERAL);
        }
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String token = request.refreshToken();

        String email = jwtService.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(400, ErrorMessage.User.EMAIL_NOT_EXISTED));

        CustomUserDetails userDetails = new CustomUserDetails(user);

        if (!jwtService.isTokenValid(token, userDetails) || jwtService.isAccessToken(token)) {
            throw new AppException(400, ErrorMessage.Auth.INVALID_REFRESH_TOKEN);
        }

        jwtService.invalidateToken(token);

        String newAccessToken = jwtService.generateToken(user, false);
        String newRefreshToken = jwtService.generateToken(user, true);

        return new LoginResponse(newAccessToken, newRefreshToken, user.getId(), CommonConstant.BEARER_TOKEN);
    }

    @Transactional
    @Override
    public void resendOtp(ResendOtpRequest request) {
        String userInfo = redisTemplate.opsForValue().get("REGISTRATION_DATA:" + request.email());
        if (userInfo == null) {
            throw new AppException(400, ErrorMessage.Auth.SESSION_EXPIRED);
        }
        String newOtp = String.format("%06d", new Random().nextInt(1000000));

        // Luu otp
        try {
            redisTemplate.opsForValue().set("REGISTRATION_OTP:" + request.email(), newOtp, 5, TimeUnit.MINUTES);
        } catch (JacksonException e) {
            throw new AppException(500, ErrorMessage.EXCEPTION_GENERAL);
        }

        // Gui email
        mailService.sendOtp(request.email(), newOtp);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(400, ErrorMessage.User.EMAIL_NOT_EXISTED));

        String otp = String.format("%06d", new Random().nextInt(1000000));

        // Luu otp
        redisTemplate.opsForValue().set("RESET_PASSWORD_OTP:" + request.email(), otp, 5, TimeUnit.MINUTES);

        // Gui email (tai su dung ham gui OTP dang co)
        mailService.sendOtp(request.email(), otp);
    }

    @Override
    public void verifyResetOtp(VerifyOtpRequest request) {
        String cachedOtp = redisTemplate.opsForValue().get("RESET_PASSWORD_OTP:" + request.getEmail());
        if (cachedOtp == null || !cachedOtp.equals(request.getOtp())) {
            throw new AppException(400, ErrorMessage.Auth.INVALID_OTP);
        }

        // OTP dung -> xoa (dung 1 lan), danh dau da xac thuc de buoc reset-password
        // khong can nhap lai OTP nua (trong 10 phut) (khop voi UX 3 buoc: email -> otp -> mat khau moi)
        redisTemplate.delete("RESET_PASSWORD_OTP:" + request.getEmail());
        redisTemplate.opsForValue().set("RESET_PASSWORD_VERIFIED:" + request.getEmail(), "true", 10, TimeUnit.MINUTES);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String verified = redisTemplate.opsForValue().get("RESET_PASSWORD_VERIFIED:" + request.email());
        if (verified == null) {
            throw new AppException(400, ErrorMessage.Auth.RESET_SESSION_EXPIRED);
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new AppException(400, ErrorMessage.PASSWORD_MISMATCH);
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(400, ErrorMessage.User.EMAIL_NOT_EXISTED));

        if (user.getPassword() != null && passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new AppException(400, ErrorMessage.Auth.PASSWORD_SAME_AS_OLD);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        // Danh dau thoi diem doi mat khau -> vo hieu hoa moi token phat truoc do (JwtService se check lai)
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);

        redisTemplate.delete("RESET_PASSWORD_VERIFIED:" + request.email());
    }
}
