package com.gateway.workers;

import com.gateway.jobs.DeliverWebhookJob;
import com.gateway.models.WebhookLog;
import com.gateway.repositories.WebhookLogRepository;
import com.gateway.services.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
@Profile("worker")
public class WebhookRetryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(WebhookRetryScheduler.class);

    @Autowired
    private WebhookLogRepository webhookLogRepository;

    @Autowired
    private JobService jobService;

    // Run every 10 seconds to check for retries
    @Scheduled(fixedDelay = 10000)
    public void checkRetries() {
        try {
            List<WebhookLog> pendingLogs = webhookLogRepository.findByStatusAndNextRetryAtBefore("pending",
                    LocalDateTime.now());

            for (WebhookLog log : pendingLogs) {
                logger.info("Re-queueing webhook for retry: {}", log.getId());

                DeliverWebhookJob job = new DeliverWebhookJob(
                        log.getMerchantId(),
                        log.getEvent(),
                        log.getPayload(),
                        log.getId());

                jobService.scheduleWebhookDelivery(job);

                // Update nextRetryAt to avoid immediate re-pickup by this polled (though
                // setting to null or tomorrow might be safer until processed,
                // but effectively we just assume it gets picked up. To be safe, we could set
                // nextRetryAt to null or future in the worker).
                // Actually the Worker will update it. But to prevent double queuing if worker
                // is slow, we should probably "lock" it or update timestamp.
                // For this simple implementation, if we run every 10s, we might double queue if
                // queue is backed up.
                // Let's bump it forward temporarily.

                log.setNextRetryAt(LocalDateTime.now().plusMinutes(1)); // Temorary bump
                webhookLogRepository.save(log);
            }
        } catch (Exception e) {
            logger.error("Error in retry scheduler", e);
        }
    }
}
