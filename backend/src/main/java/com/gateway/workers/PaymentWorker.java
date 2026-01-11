package com.gateway.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.jobs.DeliverWebhookJob;
import com.gateway.jobs.ProcessPaymentJob;
import com.gateway.jobs.ProcessPaymentJob;
import com.gateway.models.Payment;
import com.gateway.repositories.PaymentRepository;
import com.gateway.services.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class PaymentWorker {

    private static final Logger logger = LoggerFactory.getLogger(PaymentWorker.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private JobService jobService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${test.mode:false}")
    private boolean testMode;

    @Value("${test.payment.success:true}")
    private boolean testPaymentSuccess;

    @Value("${test.processing.delay:1000}")
    private long testProcessingDelay;

    public void process(ProcessPaymentJob job) {
        logger.info("Processing payment job: {}", job.getPaymentId());

        Payment payment = paymentRepository.findById(job.getPaymentId()).orElse(null);
        if (payment == null) {
            logger.error("Payment not found: {}", job.getPaymentId());
            return;
        }

        try {
            // Simulate delay
            long delay = testMode ? testProcessingDelay : 5000 + new Random().nextInt(5001);
            Thread.sleep(delay);

            boolean success = determineOutcome(payment.getMethod());

            if (success) {
                payment.setStatus("success");
                payment.setErrorCode(null);
                payment.setErrorDescription(null);
            } else {
                payment.setStatus("failed");
                payment.setErrorCode("PAYMENT_FAILED");
                payment.setErrorDescription("Payment failed due to bank rejection");
            }

            paymentRepository.save(payment);

            // Enqueue Webhook
            enqueueWebhook(payment);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Worker interrupted", e);
        } catch (Exception e) {
            logger.error("Error processing payment", e);
        }
    }

    private boolean determineOutcome(String method) {
        if (testMode)
            return testPaymentSuccess;

        Random random = new Random();
        if ("upi".equalsIgnoreCase(method)) {
            return random.nextInt(100) < 90; // 90% success
        } else {
            return random.nextInt(100) < 95; // 95% success
        }
    }

    private void enqueueWebhook(Payment payment) {
        try {
            String event = "payment." + payment.getStatus();

            // Build payload
            Map<String, Object> data = new HashMap<>();
            data.put("payment", payment);

            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("event", event);
            payloadMap.put("timestamp", System.currentTimeMillis() / 1000);
            payloadMap.put("data", data);

            String payloadJson = objectMapper.writeValueAsString(payloadMap);

            DeliverWebhookJob webhookJob = new DeliverWebhookJob(
                    payment.getMerchantId(),
                    event,
                    payloadJson,
                    null);

            jobService.scheduleWebhookDelivery(webhookJob);

        } catch (Exception e) {
            logger.error("Error enqueueing webhook", e);
        }
    }
}
