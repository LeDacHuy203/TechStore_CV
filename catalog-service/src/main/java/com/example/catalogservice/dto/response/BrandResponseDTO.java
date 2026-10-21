package com.example.catalogservice.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandResponseDTO {

    private Long id;
    private String name;
    private String logoUrl;
}
