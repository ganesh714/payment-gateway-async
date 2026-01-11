package com.gateway.jobs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliverWebhookJob {
    private UUID merchantId;
    private String event;
    private String payload;
    private UUID webhookLogId; // Optional, if retrying existing log
}
