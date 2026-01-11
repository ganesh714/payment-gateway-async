package com.gateway.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.jobs.ProcessPaymentJob;
import com.gateway.models.IdempotencyKey;
import com.gateway.models.Merchant;
import com.gateway.models.Order;
import com.gateway.models.Payment;
import com.gateway.repositories.IdempotencyKeyRepository;
import com.gateway.repositories.OrderRepository;
import com.gateway.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private JobService jobService;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public Object createPayment(Merchant merchant, Map<String, Object> payload, String idempotencyHeader) {
        // Idempotency Check
        if (idempotencyHeader != null && merchant != null) {
            Optional<IdempotencyKey> existingKey = idempotencyKeyRepository.findByKeyAndMerchantId(idempotencyHeader,
                    merchant.getId());
            if (existingKey.isPresent()) {
                IdempotencyKey key = existingKey.get();
                if (key.getExpiresAt().isAfter(LocalDateTime.now())) {
                    try {
                        return objectMapper.readValue(key.getResponse(), Payment.class);
                    } catch (JsonProcessingException e) {
                        // ignore and process as new? or error?
                    }
                } else {
                    idempotencyKeyRepository.delete(key);
                }
            }
        }

        String orderId = (String) payload.get("order_id");
        if (orderId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "order_id is required");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (merchant != null && !order.getMerchantId().equals(merchant.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found for this merchant");
        }

        String method = (String) payload.get("method");
        if (!"upi".equals(method) && !"card".equals(method)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment method");
        }

        Payment payment = new Payment();
        payment.setId(generatePaymentId());
        payment.setOrderId(orderId);
        payment.setMerchantId(order.getMerchantId());
        payment.setAmount(order.getAmount());
        payment.setCurrency(order.getCurrency());
        payment.setMethod(method);
        payment.setStatus("pending"); // Async start

        if ("upi".equals(method)) {
            String vpa = (String) payload.get("vpa");
            if (!validationService.validateVPA(vpa)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid VPA");
            }
            payment.setVpa(vpa);
        } else {
            Map<String, String> card = (Map<String, String>) payload.get("card");
            if (card == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card details required");
            }
            String number = card.get("number");
            String expMonth = card.getOrDefault("expiry_month", "");
            String expYear = card.getOrDefault("expiry_year", "");

            if (!validationService.validateLuhn(number)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Card Number");
            }
            if (!validationService.validateExpiry(expMonth, expYear)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card Expired or Invalid Date");
            }

            payment.setCardNetwork(validationService.detectCardNetwork(number));
            payment.setCardLast4(number.length() >= 4 ? number.substring(number.length() - 4) : number);
        }

        payment = paymentRepository.save(payment);

        // Enqueue Job
        ProcessPaymentJob job = new ProcessPaymentJob(payment.getId());
        jobService.schedulePaymentProcessing(job);

        // Save Idempotency Key
        if (idempotencyHeader != null && merchant != null) {
            try {
                String responseJson = objectMapper.writeValueAsString(payment);
                IdempotencyKey key = IdempotencyKey.builder()
                        .key(idempotencyHeader)
                        .merchantId(merchant.getId())
                        .response(responseJson)
                        .expiresAt(LocalDateTime.now().plusHours(24))
                        .build();
                idempotencyKeyRepository.save(key);
            } catch (JsonProcessingException e) {
                // log error
            }
        }

        return payment;
    }

    public Payment capturePayment(String paymentId, Merchant merchant, Map<String, Object> payload) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));

        if (!payment.getMerchantId().equals(merchant.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        }

        if (!"success".equals(payment.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment not in capturable state");
        }

        if (payment.isCaptured()) {
            // Already captured
            return payment;
        }

        payment.setCaptured(true);
        // payment.setUpdatedAt(LocalDateTime.now()); // Handled by @UpdateTimestamp
        return paymentRepository.save(payment);
    }

    public Payment getPayment(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    public java.util.List<Payment> getPayments(Merchant merchant) {
        return paymentRepository.findByMerchantId(merchant.getId());
    }

    private String generatePaymentId() {
        StringBuilder sb = new StringBuilder("pay_");
        for (int i = 0; i < 16; i++) {
            sb.append(ALPHABET.charAt(SECURE_RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
