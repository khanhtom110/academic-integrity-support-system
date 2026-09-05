package com.haui.lms.service.impl;

import com.haui.lms.constant.CommonConstant;
import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.dto.request.ChangePasswordRequest;
import com.haui.lms.dto.request.UpdateProfileRequest;
import com.haui.lms.dto.response.UserProfileResponse;
import com.haui.lms.entity.User;
import com.haui.lms.exception.extended.AppException;
import com.haui.lms.repository.UserRepository;
import com.haui.lms.service.CloudinaryService;
import com.haui.lms.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserProfileResponse getProfile(String email) {
        return UserProfileResponse.from(findByEmail(email));
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findByEmail(email);

        user.setFullName(request.fullName().trim());

        return UserProfileResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserProfileResponse updateAvatar(String email, MultipartFile file) {
        User user = findByEmail(email);

        // publicId dat theo id cua user nen moi lan upload se ghi de anh cu,
        // khong de lai file rac tren Cloudinary
        String avatarUrl = cloudinaryService.uploadImage(file, CommonConstant.Upload.AVATAR_FOLDER,
                user.getId().toString(), CommonConstant.Upload.AVATAR_DIMENSION);

        user.setAvatar(avatarUrl);

        return UserProfileResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserProfileResponse deleteAvatar(String email) {
        User user = findByEmail(email);

        if (!StringUtils.hasText(user.getAvatar())) {
            throw new AppException(400, ErrorMessage.User.AVATAR_NOT_EXISTED);
        }

        // Chi xoa tren Cloudinary neu anh do la anh user tu upload.
        // Avatar lay tu Google/Facebook chi la URL cua ho, khong nam tren Cloudinary
        // nen khong co gi de xoa, chi can bo URL trong DB.
        if (user.getAvatar().contains("res.cloudinary.com")) {
            cloudinaryService.deleteImage(CommonConstant.Upload.AVATAR_FOLDER, user.getId().toString());
        }

        user.setAvatar(null);

        return UserProfileResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findByEmail(email);

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
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(404, ErrorMessage.User.USER_NOT_EXISTED));
    }

}
