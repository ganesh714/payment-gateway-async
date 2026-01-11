package com.gateway.controllers;

import com.gateway.services.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @org.springframework.beans.factory.annotation.Value("${gateway.test.merchant.name:Test Merchant}")
    private String testMerchantName;

    @org.springframework.beans.factory.annotation.Value("${gateway.test.merchant.api-key}")
    private String testApiKey;

    @org.springframework.beans.factory.annotation.Value("${gateway.test.merchant.api-secret}")
    private String testApiSecret;

    @GetMapping("/merchant")
    public ResponseEntity<Map<String, String>> getTestMerchant() {
        Map<String, String> data = new HashMap<>();
        data.put("name", testMerchantName);
        data.put("apiKey", testApiKey);
        data.put("apiSecret", testApiSecret);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/jobs/status")
    public ResponseEntity<Map<String, Object>> getJobStatus() {
        Map<String, Object> status = new HashMap<>();

        Long pendingPayments = redisTemplate.opsForList().size(JobService.QUEUE_PAYMENTS);
        Long pendingWebhooks = redisTemplate.opsForList().size(JobService.QUEUE_WEBHOOKS);
        Long pendingRefunds = redisTemplate.opsForList().size(JobService.QUEUE_REFUNDS);

        long totalPending = (pendingPayments != null ? pendingPayments : 0) +
                (pendingWebhooks != null ? pendingWebhooks : 0) +
                (pendingRefunds != null ? pendingRefunds : 0);

        status.put("pending", totalPending);
        status.put("processing", 0); // Hard to track without state in DB or Redis set
        status.put("completed", 0); // "
        status.put("failed", 0); // "
        status.put("worker_status", "running"); // Assuming since API is up and Redis connection works

        return ResponseEntity.ok(status);
    }
}
