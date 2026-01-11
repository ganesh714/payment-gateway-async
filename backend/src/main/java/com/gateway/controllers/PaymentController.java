package com.gateway.controllers;

import com.gateway.models.Merchant;
import com.gateway.models.Payment;
import com.gateway.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Object> createPayment(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            Authentication authentication) {
        Merchant merchant = (Merchant) authentication.getPrincipal();
        Object response = paymentService.createPayment(merchant, payload, idempotencyKey);

        // If it's a cached response (idempotency), it might be a Payment object or the
        // raw response stored.
        // Service returns Object (Payment). Ideally we should wrap it in
        // ResponseEntity.
        // If the service handled returning cached JSON string directly, we'd return raw
        // JSON.
        // But current service implementation maps stored JSON back to Payment object.

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<Payment> capturePayment(
            @PathVariable String paymentId,
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {
        Merchant merchant = (Merchant) authentication.getPrincipal();
        Payment payment = paymentService.capturePayment(paymentId, merchant, payload);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPayment(@PathVariable String paymentId, Authentication authentication) {
        Merchant merchant = (Merchant) authentication.getPrincipal();
        Payment payment = paymentService.getPayment(paymentId);

        if (!payment.getMerchantId().equals(merchant.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        }

        return ResponseEntity.ok(payment);
    }

    @GetMapping
    public ResponseEntity<java.util.List<Payment>> getPayments(Authentication authentication) {
        Merchant merchant = (Merchant) authentication.getPrincipal();
        return ResponseEntity.ok(paymentService.getPayments(merchant));
    }

    @PostMapping("/public")
    public ResponseEntity<Object> createPaymentPublic(@RequestBody Map<String, Object> payload) {
        // Public endpoint for checkout page - no merchant auth required here
        // We pass null for idempotency key for public endpoint for now, or handle if
        // needed.
        // Requirement doesn't strictly say public endpoint needs idempotency.
        Object payment = paymentService.createPayment(null, payload, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping("/{paymentId}/public")
    public ResponseEntity<Payment> getPaymentPublic(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }
}
