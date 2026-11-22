package com.example.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String recipient;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder.Default
    @Column(nullable = false, length = 50)
    private String type = "GENERAL"; // ORDER_CONFIRMED, PAYMENT_SUCCESS, WARRANTY_ACTIVATED, GENERAL

    @Column(length = 50)
    private String referenceId; // orderCode, serialNo...

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = "SENT"; // SENT, FAILED, QUEUED

    @CreationTimestamp
    private LocalDateTime createdAt;
}
