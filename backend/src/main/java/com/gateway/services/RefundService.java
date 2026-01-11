package com.gateway.services;

import com.gateway.jobs.ProcessRefundJob;
import com.gateway.models.Payment;
import com.gateway.models.Refund;
import com.gateway.repositories.PaymentRepository;
import com.gateway.repositories.RefundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class RefundService {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private JobService jobService;

    public Refund createRefund(String paymentId, UUID merchantId, Map<String, Object> payload) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        if (!payment.getMerchantId().equals(merchantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        }

        if (!"success".equals(payment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment not in refundable state"); // But req
                                                                                                          // says Error
                                                                                                          // Code:
                                                                                                          // BAD_REQUEST_ERROR
        }

        Integer amount = (Integer) payload.get("amount");
        String reason = (String) payload.get("reason");

        if (amount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount is required");
        }

        Long totalRefunded = refundRepository.sumProcessedOrPendingAmountByPaymentId(paymentId);
        if (totalRefunded == null)
            totalRefunded = 0L;

        if (amount + totalRefunded > payment.getAmount()) {
            // throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refund amount
            // exceeds available amount");
            // To match specific error structure, Controller might handle exception
            // translation or we throw custom.
            // For now standard ResponseStatusException.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refund amount exceeds available amount");
        }

        Refund refund = Refund.builder()
                .id(generateRefundId())
                .paymentId(paymentId)
                .merchantId(merchantId)
                .amount(amount.longValue())
                .reason(reason)
                .status("pending")
                .createdAt(LocalDateTime.now())
                .build();

        refund = refundRepository.save(refund);

        ProcessRefundJob job = new ProcessRefundJob(refund.getId());
        jobService.scheduleRefundProcessing(job);

        return refund;
    }

    public Refund getRefund(String refundId) {
        return refundRepository.findById(refundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Refund not found"));
    }

    private String generateRefundId() {
        StringBuilder sb = new StringBuilder("rfnd_");
        for (int i = 0; i < 16; i++) {
            sb.append(ALPHABET.charAt(SECURE_RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
