package com.gateway.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "webhook_logs", indexes = {
        @Index(name = "idx_webhook_merchant", columnList = "merchant_id"),
        @Index(name = "idx_webhook_status", columnList = "status"),
        @Index(name = "idx_webhook_retry", columnList = "next_retry_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(length = 50, nullable = false)
    private String event;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(length = 20, nullable = false)
    private String status; // pending, success, failed

    @Column(nullable = false)
    private Integer attempts;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "response_code")
    private Integer responseCode;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (attempts == null) {
            attempts = 0;
        }
        if (status == null) {
            status = "pending";
        }
    }
}
