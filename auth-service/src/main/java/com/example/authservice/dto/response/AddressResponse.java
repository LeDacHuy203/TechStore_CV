package com.example.authservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private String province;
    private String district;
    private String ward;
    private String detailAddress;
//    private boolean isDefault;
}
