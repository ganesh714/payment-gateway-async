package com.gateway.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.jobs.DeliverWebhookJob;
import com.gateway.jobs.ProcessPaymentJob;
import com.gateway.jobs.ProcessRefundJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class JobService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public static final String QUEUE_PAYMENTS = "queue:payments";
    public static final String QUEUE_WEBHOOKS = "queue:webhooks";
    public static final String QUEUE_REFUNDS = "queue:refunds";

    public void schedulePaymentProcessing(ProcessPaymentJob job) {
        try {
            String json = objectMapper.writeValueAsString(job);
            redisTemplate.opsForList().rightPush(QUEUE_PAYMENTS, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing payment job", e);
        }
    }

    public void scheduleWebhookDelivery(DeliverWebhookJob job) {
        try {
            String json = objectMapper.writeValueAsString(job);
            redisTemplate.opsForList().rightPush(QUEUE_WEBHOOKS, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing webhook job", e);
        }
    }

    public void scheduleRefundProcessing(ProcessRefundJob job) {
        try {
            String json = objectMapper.writeValueAsString(job);
            redisTemplate.opsForList().rightPush(QUEUE_REFUNDS, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing refund job", e);
        }
    }
}
