package com.haui.lms.controller;

import com.haui.lms.base.ApiResponse;
import com.haui.lms.mock.UserResponseDto;
import com.haui.lms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/users")
@Tag(name = "User Controller")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

//    @Operation(summary = "Lay thong tin chi tiet profile", description = "Dung de nguoi dung lay thong tin profile day du", security = @SecurityRequirement(name = "Bearer Token"))
//    @GetMapping(value = "/")
//    public ResponseEntity<ApiResponse<UserResponseDto>> getMyProfile(@AuthenticationPrincipal CustomerUserDetails userDetails){
//        String userId = userDetail.getUsername();
//    }
}
