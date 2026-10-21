package com.example.authservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {

    private String accessToken; //Token ngắn hạn (dùng để xác thực các request đến server)
    private String refreshToken; //Token dài hạn (dùng để xin cấp lại access token khi access token hết hạn)

    @Builder.Default
    private String tokenType = "Bearer"; // Tiêu chuẩn token type, mặc định là "Bearer"
    private long expiresIn; // Thời gian sống của access token (tính theo giây, ví dụ 3600 = 1 giờ)
}
