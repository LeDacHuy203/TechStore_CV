package com.example.authservice.dto.response;

import com.example.authservice.entity.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
    // Danh sách các địa chỉ nhận hàng của người dùng này
    private List<AddressResponse> addresses;
}
