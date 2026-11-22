package com.example.inventoryservice.dto.response;

import com.example.inventoryservice.entity.SerialStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SerialNumberResponse {

    private Long id;
    private String serialCode;
    private String productSku;
    private Long orderId;
    private SerialStatus status;
    private LocalDateTime warrantyStartDate;
    private LocalDateTime warrantyEndDate;
}
