package com.haui.lms.controller;

import com.haui.lms.base.ApiResponse;
import com.haui.lms.constant.ApiPath;
import com.haui.lms.constant.SuccessMessage;
import com.haui.lms.constant.UrlConstant;
import com.haui.lms.dto.request.UpdateProfileRequest;
import com.haui.lms.dto.response.UserProfileResponse;
import com.haui.lms.security.CustomUserDetails;
import com.haui.lms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping(ApiPath.API_V1)
@Tag(name = "User", description = "Các API quản lý hồ sơ cá nhân")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Xem hồ sơ cá nhân", description = "Trả về thông tin của chính người dùng đang đăng nhập")
    @GetMapping(UrlConstant.User.GET_PROFILE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UserProfileResponse response = userService.getProfile(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(SuccessMessage.User.GET_PROFILE_SUCCESS, response));
    }

    @Operation(summary = "Cập nhật hồ sơ cá nhân", description = "Chỉ cập nhật họ tên. Email là định danh tài khoản nên không cho phép thay đổi")
    @PutMapping(UrlConstant.User.UPDATE_PROFILE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = userService.updateProfile(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.ok(SuccessMessage.User.UPDATE_PROFILE_SUCCESS, response));
    }

    @Operation(summary = "Cập nhật ảnh đại diện", description = "Tải lên ảnh mới (JPEG/PNG/WebP, tối đa 5MB). Ảnh cũ sẽ bị ghi đè")
    @PostMapping(value = UrlConstant.User.AVATAR, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateAvatar(
            @AuthenticationPrincipal CustomUserDetails principal, @RequestPart("file") MultipartFile file) {
        UserProfileResponse response = userService.updateAvatar(principal.getUsername(), file);
        return ResponseEntity.ok(ApiResponse.ok(SuccessMessage.User.UPDATE_AVATAR_SUCCESS, response));
    }

    @Operation(summary = "Xóa ảnh đại diện", description = "Gỡ ảnh đại diện hiện tại, quay về ảnh mặc định do frontend hiển thị")
    @DeleteMapping(UrlConstant.User.AVATAR)
    public ResponseEntity<ApiResponse<UserProfileResponse>> deleteAvatar(
            @AuthenticationPrincipal CustomUserDetails principal) {
        UserProfileResponse response = userService.deleteAvatar(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(SuccessMessage.User.DELETE_AVATAR_SUCCESS, response));
    }
}
