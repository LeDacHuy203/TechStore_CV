package com.example.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarrantyActivationRequest {

    @NotBlank(message = "Mã Serial không được để trống")
    private String serialCode;

    @NotNull(message = "Mã đơn hàng không được để trống")
    private Long orderId;

    @Min(value = 1, message = "Thời hạn bảo hành tối thiểu 1 tháng")
    @Builder.Default
    private int warrantyMonths = 24; // Mặc định 24 tháng cho laptop
}
