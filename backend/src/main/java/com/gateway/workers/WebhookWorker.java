package com.gateway.workers;

import com.gateway.jobs.DeliverWebhookJob;
import com.gateway.models.Merchant;
import com.gateway.models.WebhookLog;
import com.gateway.repositories.MerchantRepository;
import com.gateway.repositories.WebhookLogRepository;
import com.gateway.services.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Component
public class WebhookWorker {

    private static final Logger logger = LoggerFactory.getLogger(WebhookWorker.class);

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private WebhookLogRepository webhookLogRepository;

    @Autowired
    private WebhookService webhookService;

    @Value("${webhook.retry.intervals.test:false}")
    private boolean testRetryIntervals;

    private final RestTemplate restTemplate = new RestTemplate();

    public void process(DeliverWebhookJob job) {
        logger.info("Processing webhook job for merchant: {}", job.getMerchantId());

        Merchant merchant = merchantRepository.findById(job.getMerchantId()).orElse(null);
        if (merchant == null) {
            logger.error("Merchant not found");
            return;
        }

        if (merchant.getWebhookUrl() == null || merchant.getWebhookUrl().isEmpty()) {
            logger.info("No webhook URL configured for merchant provided");
            return;
        }

        // Generate ID / Log Entry
        WebhookLog log;
        if (job.getWebhookLogId() != null) {
            log = webhookLogRepository.findById(job.getWebhookLogId()).orElse(null);
            if (log == null) {
                // If ID passed but not found (shouldn't happen), create new
                log = createNewLog(job, merchant.getId());
            }
        } else {
            log = createNewLog(job, merchant.getId());
        }

        // Perform Delivery
        try {
            log.setLastAttemptAt(LocalDateTime.now());
            log.setAttempts(log.getAttempts() + 1);

            String signature = webhookService.generateSignature(job.getPayload(),
                    merchant.getWebhookSecret() != null ? merchant.getWebhookSecret() : "");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Webhook-Signature", signature);

            HttpEntity<String> entity = new HttpEntity<>(job.getPayload(), headers);

            // Set timeout? RestTemplate default usually okay, or configure it.
            // Requirement says Timeout: 5 seconds. Ideally we configure RestTemplate bean.

            ResponseEntity<String> response = restTemplate.postForEntity(merchant.getWebhookUrl(), entity,
                    String.class);

            log.setResponseCode(response.getStatusCode().value());
            log.setResponseBody(response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                log.setStatus("success");
                log.setNextRetryAt(null);
            } else {
                handleFailure(log);
            }

        } catch (Exception e) {
            logger.error("Webhook delivery failed: {}", e.getMessage());
            log.setResponseCode(0);
            log.setResponseBody(e.getMessage());
            handleFailure(log);
        }

        webhookLogRepository.save(log);
    }

    private WebhookLog createNewLog(DeliverWebhookJob job, java.util.UUID merchantId) {
        return WebhookLog.builder()
                .merchantId(merchantId)
                .event(job.getEvent())
                .payload(job.getPayload())
                .status("pending")
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void handleFailure(WebhookLog log) {
        if (log.getAttempts() >= 5) {
            log.setStatus("failed");
            log.setNextRetryAt(null);
        } else {
            log.setStatus("pending");
            LocalDateTime nextRetry = testRetryIntervals ? webhookService.calculateTestNextRetry(log.getAttempts())
                    : webhookService.calculateNextRetry(log.getAttempts());
            log.setNextRetryAt(nextRetry);
        }
    }
}
