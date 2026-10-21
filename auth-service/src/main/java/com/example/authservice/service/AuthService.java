package com.example.authservice.service;

import com.example.authservice.config.JwtTokenProvider;
import com.example.authservice.dto.request.LoginRequest;
import com.example.authservice.dto.request.RegisterRequest;
import com.example.authservice.dto.response.TokenResponse;
import com.example.authservice.entity.Role;
import com.example.authservice.entity.User;
import com.example.authservice.exception.AppException;
import com.example.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    // 1. Nghiệp vụ Đăng ký tài khoản
    public void register(RegisterRequest req) {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new AppException(400, "Tên đăng nhập đã tồn tại trong hệ thống!");
        }

        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AppException(400, "Email này đã được sử dụng bởi tài khoản khác!");
        }

        // Tạo User mới và băm mật khẩu với BCrypt
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword())) // Băm mật khẩu 1 chiều
                .fullName(req.getFullName())
                .phoneNumber(req.getPhoneNumber())
                .role(Role.ROLE_CUSTOMER) // Mặc định là quyền Khách hàng
                .active(true)
                .build();

        userRepository.save(user);
    }

    // 2. Nghiệp vụ Đăng nhập
    public TokenResponse login(LoginRequest req) {
        // Tìm User theo username hoặc email
        User user = userRepository.findByUsername(req.getUsername())
                .or(() -> userRepository.findByEmail(req.getUsername()))
                .orElseThrow(() -> new AppException(400, "Tài khoản hoặc mật khẩu không chính xác!"));

        // Kiểm tra xem tài khoản có đang hoạt động không
        if (!user.isActive()) {
            throw new AppException(403, "Tài khoản của bạn đã bị khóa!");
        }

        // So khớp mật khẩu người dùng nhập với mật khẩu đã băm trong database
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new AppException(400, "Tài khoản hoặc mật khẩu không chính xác!");
        }

        // Sinh cặp Token JWT
        String accessToken = tokenProvider.generateAccessToken(user.getUsername(), user.getRole().name());
        String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpirationInSeconds())
                .build();
    }

    // 3. Nghiệp vụ Làm mới Token (Refresh Token)
    public TokenResponse refreshToken(String refreshToken) {
        // Kiểm tra tính hợp lệ của Refresh Token
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new AppException(401, "Refresh Token không hợp lệ hoặc đã hết hạn!");
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy thông tin tài khoản!"));

        // Cấp Access Token mới
        String newAccessToken = tokenProvider.generateAccessToken(user.getUsername(), user.getRole().name());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpirationInSeconds())
                .build();
    }
}