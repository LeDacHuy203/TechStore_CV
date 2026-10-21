package com.example.authservice.controller;

import com.example.authservice.dto.response.ApiResponse;
import com.example.authservice.dto.response.UserResponse;
import com.example.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // API Lấy thông tin cá nhân của tài khoản hiện tại (yêu cầu gửi kèm Bearer Token)
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@AuthenticationPrincipal String currentUsername) {
        UserResponse response = userService.getProfile(currentUsername);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thông tin tài khoản thành công!"));
    }
}