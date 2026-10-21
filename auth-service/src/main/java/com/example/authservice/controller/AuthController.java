package com.example.authservice.controller;

import com.example.authservice.dto.request.LoginRequest;
import com.example.authservice.dto.request.RegisterRequest;
import com.example.authservice.dto.response.ApiResponse;
import com.example.authservice.dto.response.TokenResponse;
import com.example.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 1. API Đăng ký tài khoản
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký tài khoản thành công!"));
    }

    // 2. API Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest req) {
        TokenResponse tokens = authService.login(req);
        return ResponseEntity.ok(ApiResponse.success(tokens, "Đăng nhập thành công!"));
    }

    // 3. API Làm mới Access Token
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        TokenResponse tokens = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(tokens, "Làm mới Token thành công!"));
    }
}