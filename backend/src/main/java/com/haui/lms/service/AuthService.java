package com.haui.lms.service;

import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.dto.request.FacebookLoginRequest;
import com.haui.lms.dto.request.GoogleLoginRequest;
import com.haui.lms.dto.request.OutlookLoginRequest;
import com.haui.lms.dto.response.AuthResponse;
import com.haui.lms.dto.response.FacebookUserInfoResponse;
import com.haui.lms.dto.response.GoogleUserInfoResponse;
import com.haui.lms.dto.response.OutlookUserResponse;
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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final GoogleOAuth2Service googleOAuth2Service;
    private final FacebookOAuth2Service facebookOAuth2Service;
    private final OutlookOAuth2Service outlookOAuth2Service;
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

        User user = resolveOAuthUser(AuthProvider.GOOGLE, googleProfile.sub(), googleProfile.email(),
                googleProfile.name(), googleProfile.picture());

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse loginWithFacebook(FacebookLoginRequest request) {
        FacebookUserInfoResponse fbProfile = facebookOAuth2Service.getUserInfo(request.code());

        if (!StringUtils.hasText(fbProfile.email())) {
            log.error("Facebook account missing email. FB_ID: {}", fbProfile.id());
            throw new AppException(400, ErrorMessage.Auth.FACEBOOK_EMAIL_MISSING);
        }

        User user = resolveOAuthUser(AuthProvider.FACEBOOK, fbProfile.id(), fbProfile.email(), fbProfile.name(),
                fbProfile.getAvatarUrl());

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse loginWithOutlook(OutlookLoginRequest request) {
        OutlookUserResponse outlookProfile = outlookOAuth2Service.getUserProfile(request.code());

        if (!StringUtils.hasText(outlookProfile.getEmail())) {
            log.error("Outlook account missing email. Outlook_ID: {}", outlookProfile.id());
            throw new AppException(400, ErrorMessage.Auth.OUTLOOK_EMAIL_MISSING);
        }

        // Microsoft Graph /me khong tra ve URL anh dai dien (phai goi rieng /me/photo
        // va nhan ve du lieu nhi phan) nen de null, user tu upload sau
        User user = resolveOAuthUser(AuthProvider.OUTLOOK, outlookProfile.id(), outlookProfile.getEmail(),
                outlookProfile.displayName(), null);

        return generateAuthResponse(user);
    }

    /**
     * Tim user tuong ung voi tai khoan OAuth theo thu tu uu tien:
     * <ol>
     * <li>Tra theo (provider, providerUserId): ma dinh danh nay do provider cap va
     * khong bao gio doi, ke ca khi user doi email ben phia Google/Facebook</li>
     * <li>Chua lien ket thi tra theo email de gan them provider vao tai khoan da co
     * (vi du user dang ky bang email roi sau do dang nhap bang Google)</li>
     * <li>Khong tim thay gi thi tao user moi</li>
     * </ol>
     */
    private User resolveOAuthUser(AuthProvider provider, String providerUserId, String email, String fullName,
            String avatarUrl) {
        Optional<UserOAuthAccount> linkedAccount = userOAuthAccountRepository.findByProviderAndProviderUserId(provider,
                providerUserId);
        if (linkedAccount.isPresent()) {
            return linkedAccount.get().getUser();
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            linkOAuthAccountToUser(user, provider, providerUserId);
            return user;
        }

        return createNewOAuthUser(email, fullName, provider, providerUserId, avatarUrl);
    }

    private User createNewOAuthUser(String email, String fullName, AuthProvider provider, String providerUserId,
            String avatarUrl) {
        // Chi lay avatar cua provider lam anh mac dinh o lan tao tai khoan dau tien.
        // Nhung lan dang nhap sau khong ghi de, de ton trong anh user tu chon.
        User newUser = User.builder().email(email).fullName(fullName).avatar(avatarUrl).role(Role.USER).isActive(true)
                .build();

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