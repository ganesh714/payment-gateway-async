package com.gateway.repositories;

import com.gateway.models.WebhookLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WebhookLogRepository extends JpaRepository<WebhookLog, UUID> {
    Page<WebhookLog> findByMerchantId(UUID merchantId, Pageable pageable);

    List<WebhookLog> findByStatusAndNextRetryAtBefore(String status, LocalDateTime timestamp);
}
