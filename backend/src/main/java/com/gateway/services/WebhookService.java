package com.gateway.services;

import com.gateway.models.WebhookLog;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Service
public class WebhookService {

    private static final String HMAC_SHA256 = "HmacSHA256";

    public String generateSignature(String payload, String secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating signature", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public LocalDateTime calculateNextRetry(int currentAttempt) {
        // Attempt 1: Immediate (handled by caller before first retry scheduled)
        // Retry logic starts AFTER first failure, so attempts=1 means we are scheduling
        // for 2nd attempt
        // Requirements:
        // Attempt 2: After 1 minute
        // Attempt 3: After 5 minutes
        // Attempt 4: After 30 minutes
        // Attempt 5: After 2 hours

        switch (currentAttempt) {
            case 1:
                return LocalDateTime.now().plusMinutes(1);
            case 2:
                return LocalDateTime.now().plusMinutes(5);
            case 3:
                return LocalDateTime.now().plusMinutes(30);
            case 4:
                return LocalDateTime.now().plusHours(2);
            default:
                return null; // No more retries
        }
    }

    public LocalDateTime calculateTestNextRetry(int currentAttempt) {
        // Allow faster retries for testing
        switch (currentAttempt) {
            case 1:
                return LocalDateTime.now().plusSeconds(5);
            case 2:
                return LocalDateTime.now().plusSeconds(10);
            case 3:
                return LocalDateTime.now().plusSeconds(15);
            case 4:
                return LocalDateTime.now().plusSeconds(20);
            default:
                return null;
        }
    }
}
