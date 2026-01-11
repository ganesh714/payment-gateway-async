package com.gateway.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.jobs.DeliverWebhookJob;
import com.gateway.jobs.ProcessPaymentJob;
import com.gateway.jobs.ProcessRefundJob;
import com.gateway.services.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Profile("worker")
public class WorkerRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(WorkerRunner.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentWorker paymentWorker;

    @Autowired
    private WebhookWorker webhookWorker;

    @Autowired
    private RefundWorker refundWorker;

    private boolean running = true;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting Worker Service...");

        ExecutorService executor = Executors.newFixedThreadPool(3);

        executor.submit(() -> processQueue(JobService.QUEUE_PAYMENTS, this::handlePaymentJob));
        executor.submit(() -> processQueue(JobService.QUEUE_WEBHOOKS, this::handleWebhookJob));
        executor.submit(() -> processQueue(JobService.QUEUE_REFUNDS, this::handleRefundJob));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }

    private interface JobHandler {
        void handle(String payload) throws Exception;
    }

    private void processQueue(String queueName, JobHandler handler) {
        logger.info("Listening on queue: {}", queueName);
        while (running) {
            try {
                // BLPOP with timeout (e.g., 5 seconds) to check running flag periodically
                // RedisTemplate doesn't have direct BLPOP, use execute
                // For simplicity in this demo, accessing opsForList().leftPop(key, timeout,
                // unit)

                String payload = redisTemplate.opsForList().leftPop(queueName, 2, TimeUnit.SECONDS);

                if (payload != null) {
                    try {
                        handler.handle(payload);
                    } catch (Exception e) {
                        logger.error("Error handling job from " + queueName, e);
                    }
                }
            } catch (Exception e) {
                logger.error("Error polling queue " + queueName, e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private void handlePaymentJob(String payload) throws Exception {
        ProcessPaymentJob job = objectMapper.readValue(payload, ProcessPaymentJob.class);
        paymentWorker.process(job);
    }

    private void handleWebhookJob(String payload) throws Exception {
        DeliverWebhookJob job = objectMapper.readValue(payload, DeliverWebhookJob.class);
        webhookWorker.process(job);
    }

    private void handleRefundJob(String payload) throws Exception {
        ProcessRefundJob job = objectMapper.readValue(payload, ProcessRefundJob.class);
        refundWorker.process(job);
    }
}
