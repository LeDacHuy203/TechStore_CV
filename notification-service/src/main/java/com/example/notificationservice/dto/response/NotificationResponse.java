package com.example.notificationservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private String recipient;
    private String subject;
    private String content;
    private String type;
    private String referenceId;
    private String status;
    private LocalDateTime createdAt;
}
