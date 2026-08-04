package com.haui.lms.service;

import com.haui.lms.dto.request.GoogleLoginRequest;
import com.haui.lms.dto.response.AuthResponse;
import com.haui.lms.dto.response.GoogleUserInfoResponse;
import com.haui.lms.entity.AuthProvider;
import com.haui.lms.entity.Role;
import com.haui.lms.entity.User;
import com.haui.lms.entity.UserOAuthAccount;
import com.haui.lms.repository.UserOAuthAccountRepository;
import com.haui.lms.repository.UserRepository;
import com.haui.lms.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.exception.extended.AppException;
import com.haui.lms.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final GoogleOAuth2Service googleOAuth2Service;
    private final UserOAuthAccountRepository userOAuthAccountRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleUserInfoResponse googleProfile = googleOAuth2Service.getUserInfo(request.code());

        // Tìm User trong DB theo Email, nếu chưa có thì tự động đăng ký mới
        User user = userRepository.findByEmail(googleProfile.email())
                .orElseGet(() -> createNewGoogleUser(googleProfile));

        // Kiểm tra xem User đã liên kết với tài khoản Google này chưa
        boolean hasOAuthLink = userOAuthAccountRepository.existsByProviderAndProviderUserId(AuthProvider.GOOGLE,
                googleProfile.sub());
        if (!hasOAuthLink) {
            linkGoogleAccountToUser(user, googleProfile.sub());
        }

        // Tạo cặp Access Token & Refresh Token bằng JwtService
        String accessToken = jwtService.generateToken(user, false);
        String refreshToken = jwtService.generateToken(user, true);

        return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    private User createNewGoogleUser(GoogleUserInfoResponse googleProfile) {

        User newUser = User.builder().email(googleProfile.email()).fullName(googleProfile.name()).role(Role.USER)
                .isActive(true).build();

        User savedUser = userRepository.save(newUser);

        // Liên kết bản ghi OAuth
        linkGoogleAccountToUser(savedUser, googleProfile.sub());

        return savedUser;
    }

    private void linkGoogleAccountToUser(User user, String providerUserId) {
        UserOAuthAccount oAuthAccount = UserOAuthAccount.builder().user(user).provider(AuthProvider.GOOGLE)
                .providerUserId(providerUserId).build();

        userOAuthAccountRepository.save(oAuthAccount);
    }

    public String generateTestToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(404, ErrorMessage.User.USER_NOT_EXISTED));
        return jwtService.generateToken(user, false);
    }

}