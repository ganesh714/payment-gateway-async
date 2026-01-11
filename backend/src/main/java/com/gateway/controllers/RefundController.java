package com.gateway.controllers;

import com.gateway.models.Merchant;
import com.gateway.models.Refund;
import com.gateway.services.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class RefundController {

    @Autowired
    private RefundService refundService;

    @PostMapping("/payments/{paymentId}/refunds")
    public ResponseEntity<Refund> createRefund(
            @PathVariable String paymentId,
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {

        Merchant merchant = (Merchant) authentication.getPrincipal();
        Refund refund = refundService.createRefund(paymentId, merchant.getId(), payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(refund);
    }

    @GetMapping("/refunds/{refundId}")
    public ResponseEntity<Refund> getRefund(
            @PathVariable String refundId,
            Authentication authentication) {
        // Validation of merchant ownership of refund?
        // Req says "GET /api/v1/refunds/{refund_id} ... Response 200"
        // Implicitly we should check if this refund belongs to the authenticated
        // merchant.

        Merchant merchant = (Merchant) authentication.getPrincipal();
        Refund refund = refundService.getRefund(refundId);

        if (!refund.getMerchantId().equals(merchant.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Refund not found");
        }

        return ResponseEntity.ok(refund);
    }
}
