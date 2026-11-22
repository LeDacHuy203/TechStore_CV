package com.example.notificationservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailNotificationRequest {

    @NotBlank(message = "Email người nhận không được để trống")
    @Email(message = "Email người nhận không đúng định dạng")
    private String recipient;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String subject;

    @NotBlank(message = "Nội dung thông báo không được để trống")
    private String content;

    private String type;

    private String referenceId;
}
