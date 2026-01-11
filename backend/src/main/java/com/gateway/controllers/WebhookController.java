package com.gateway.controllers;

import com.gateway.jobs.DeliverWebhookJob;
import com.gateway.models.Merchant;
import com.gateway.models.WebhookLog;
import com.gateway.repositories.WebhookLogRepository;
import com.gateway.services.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    @Autowired
    private WebhookLogRepository webhookLogRepository;

    @Autowired
    private JobService jobService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getWebhooks(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            Authentication authentication) {

        Merchant merchant = (Merchant) authentication.getPrincipal();

        // Offset in PageRequest is page number (index), not item offset.
        // But req says "offset=0". If it means page index 0, fine.
        // If it means item offset (SQL OFFSET), we must divide.
        // Assuming page index based on standard Spring Data practice, but standard REST
        // offset usually implies items.
        // Let's assume pagination: Page 0, Size 10.
        // If offset is passed as 0, it's page 0. If offset=10 (items), it's page 1.
        int page = offset / limit;

        Page<WebhookLog> logs = webhookLogRepository.findByMerchantId(
                merchant.getId(),
                PageRequest.of(page, limit, Sort.by("createdAt").descending()));

        Map<String, Object> response = new HashMap<>();
        response.put("data", logs.getContent());
        response.put("total", logs.getTotalElements());
        response.put("limit", limit);
        response.put("offset", offset);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{webhookId}/retry")
    public ResponseEntity<Map<String, Object>> retryWebhook(
            @PathVariable UUID webhookId,
            Authentication authentication) {

        Merchant merchant = (Merchant) authentication.getPrincipal();
        WebhookLog log = webhookLogRepository.findById(webhookId)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,
                        "Webhook log not found"));

        if (!log.getMerchantId().equals(merchant.getId())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Webhook log not found");
        }

        log.setStatus("pending");
        log.setAttempts(0);
        log.setNextRetryAt(null); // Will be picked up or immediate?
        // Req: "Reset attempts to 0, set status to 'pending', enqueue
        // DeliverWebhookJob."

        webhookLogRepository.save(log);

        DeliverWebhookJob job = new DeliverWebhookJob(
                merchant.getId(),
                log.getEvent(),
                log.getPayload(),
                log.getId());
        jobService.scheduleWebhookDelivery(job);

        Map<String, Object> response = new HashMap<>();
        response.put("id", log.getId());
        response.put("status", "pending");
        response.put("message", "Webhook retry scheduled");

        return ResponseEntity.ok(response);
    }
}
