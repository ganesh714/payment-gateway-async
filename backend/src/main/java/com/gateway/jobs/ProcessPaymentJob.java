package com.gateway.jobs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentJob {
    private String paymentId;

    // We can use a simple identifier for the job type in the JSON payload
    // but typically we'll serialize the whole object.
    // For simplicity with Redis List, we'll serialize this class to JSON.
}
