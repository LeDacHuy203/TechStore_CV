package com.example.authservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // 1. Đọc các giá trị từ application.yaml (hỗ trợ cả jwt.* và spring.jwt.*)
    @Value("${jwt.secret-key:${spring.jwt.secret-key:QuacCdqCIl6AKN81iD1bO1SedjIUTXC5EBmwiJ7L7Za}}")
    private String secretKeyString;

    @Value("${jwt.access-token-expiration:${spring.jwt.access-token-expiration:900000}}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:${spring.jwt.refresh-token-expiration:604800000}}")
    private long refreshTokenExpiration;

    private SecretKey key;

    // 2. Chạy ngay sau khi Spring inject xong các giá trị @Value
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    // 3. Tạo Access Token (15 phút)
    public String generateAccessToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    // 4. Tạo Refresh Token (7 ngày)
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    // 5. Trích xuất username từ Token
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    // 6. Kiểm tra tính hợp lệ của Token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public long getAccessTokenExpirationInSeconds() {
        return accessTokenExpiration / 1000;
    }
}
