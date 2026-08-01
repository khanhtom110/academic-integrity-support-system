package com.haui.lms.service;

import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.dto.request.FacebookLoginRequest;
import com.haui.lms.dto.request.GoogleLoginRequest;
import com.haui.lms.dto.response.AuthResponse;
import com.haui.lms.dto.response.FacebookUserInfoResponse;
import com.haui.lms.dto.response.GoogleUserInfoResponse;
import com.haui.lms.entity.AuthProvider;
import com.haui.lms.entity.Role;
import com.haui.lms.entity.User;
import com.haui.lms.entity.UserOAuthAccount;
import com.haui.lms.exception.extended.AppException;
import com.haui.lms.repository.UserOAuthAccountRepository;
import com.haui.lms.repository.UserRepository;
import com.haui.lms.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final GoogleOAuth2Service googleOAuth2Service;
    private final FacebookOAuth2Service facebookOAuth2Service;
    private final UserRepository userRepository;
    private final UserOAuthAccountRepository userOAuthAccountRepository;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleUserInfoResponse googleProfile = googleOAuth2Service.getUserInfo(request.code());

        if (!StringUtils.hasText(googleProfile.email())) {
            log.error("Google account missing email. Google_Sub: {}", googleProfile.sub());
            throw new AppException(400, ErrorMessage.Auth.GOOGLE_EMAIL_MISSING);
        }

        // Tim User co hay chua, chua thi tao moi
        User user = userRepository.findByEmail(googleProfile.email())
                .orElseGet(() -> createNewOAuthUser(googleProfile.email(), googleProfile.name(), AuthProvider.GOOGLE,
                        googleProfile.sub()));

        // Kiem tra da lien ket hay chua
        boolean hasOAuthLink = userOAuthAccountRepository.existsByProviderAndProviderUserId(AuthProvider.GOOGLE,
                googleProfile.sub());
        if (!hasOAuthLink) {
            linkOAuthAccountToUser(user, AuthProvider.GOOGLE, googleProfile.sub());
        }

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse loginWithFacebook(FacebookLoginRequest request) {
        FacebookUserInfoResponse fbProfile = facebookOAuth2Service.getUserInfo(request.code());

        if (!StringUtils.hasText(fbProfile.email())) {
            log.error("Facebook account missing email. FB_ID: {}", fbProfile.id());
            throw new AppException(400, ErrorMessage.Auth.FACEBOOK_EMAIL_MISSING);
        }

        // Tim User, chua co thi tao moi
        User user = userRepository.findByEmail(fbProfile.email()).orElseGet(
                () -> createNewOAuthUser(fbProfile.email(), fbProfile.name(), AuthProvider.FACEBOOK, fbProfile.id()));

        // Kiem tra user da lien ket hay chua
        boolean hasOAuthLink = userOAuthAccountRepository.existsByProviderAndProviderUserId(AuthProvider.FACEBOOK,
                fbProfile.id());

        if (!hasOAuthLink) {
            linkOAuthAccountToUser(user, AuthProvider.FACEBOOK, fbProfile.id());
        }

        return generateAuthResponse(user);
    }

    private User createNewOAuthUser(String email, String fullName, AuthProvider provider, String providerUserId) {
        User newUser = User.builder().email(email).fullName(fullName).role(Role.USER).isActive(true).build();

        User savedUser = userRepository.save(newUser);

        // Tao ban ghi
        linkOAuthAccountToUser(savedUser, provider, providerUserId);

        return savedUser;
    }

    private void linkOAuthAccountToUser(User user, AuthProvider provider, String providerUserId) {
        UserOAuthAccount oAuthAccount = UserOAuthAccount.builder().user(user).provider(provider)
                .providerUserId(providerUserId).build();

        userOAuthAccountRepository.save(oAuthAccount);
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user, false);
        String refreshToken = jwtService.generateToken(user, true);

        return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

}