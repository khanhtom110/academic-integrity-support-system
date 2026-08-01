package com.haui.lms.user.service.impl;

import com.haui.lms.constant.CommonConstant;
import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.dto.UserProfileResponse;
import com.haui.lms.entity.User;
import com.haui.lms.exception.extended.AppException;
import com.haui.lms.integration.otp.OtpPurpose;
import com.haui.lms.integration.otp.OtpService;
import com.haui.lms.repository.UserRepository;
import com.haui.lms.user.dto.request.ChangePasswordRequest;
import com.haui.lms.user.dto.request.ResetPasswordRequest;
import com.haui.lms.user.dto.request.UpdateProfileRequest;
import com.haui.lms.user.mapper.UserMapper;
import com.haui.lms.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    private static final Pattern PHONE_PATTERN = Pattern.compile(CommonConstant.PHONE_REGEX);

    @Override
    public UserProfileResponse getProfile(UUID userId) {
        return UserMapper.toProfileResponse(getUserOrThrow(userId));
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = getUserOrThrow(userId);

        if (StringUtils.hasText(request.fullName())) {
            user.setFullName(request.fullName());
        }
        if (StringUtils.hasText(request.phone())) {
            if (!PHONE_PATTERN.matcher(request.phone()).matches()) {
                throw new AppException(400, ErrorMessage.INVALID_FORMAT_PHONE);
            }
            user.setPhone(request.phone());
        }
        if (StringUtils.hasText(request.address())) {
            user.setAddress(request.address());
        }

        return UserMapper.toProfileResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = getUserOrThrow(userId);

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new AppException(400, ErrorMessage.Auth.INVALID_PASSWORD);
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new AppException(400, ErrorMessage.PASSWORD_MISMATCH);
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new AppException(400, ErrorMessage.Auth.PASSWORD_SAME_AS_OLD);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public void requestPasswordReset(String email) {
        if (userRepository.existsByEmail(email)) {
            otpService.generateAndSendOtp(email, OtpPurpose.RESET_PASSWORD);
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new AppException(400, ErrorMessage.PASSWORD_MISMATCH);
        }

        otpService.verifyOtp(request.email(), request.otp(), OtpPurpose.RESET_PASSWORD);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(404, ErrorMessage.User.USER_NOT_EXISTED));

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new AppException(400, ErrorMessage.Auth.PASSWORD_SAME_AS_OLD);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(404, ErrorMessage.User.USER_NOT_EXISTED));
    }
}
