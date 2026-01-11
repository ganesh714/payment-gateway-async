package com.gateway.controllers;

import com.gateway.models.Merchant;
import com.gateway.repositories.MerchantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.security.SecureRandom;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {

    @Autowired
    private MerchantRepository merchantRepository;

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @GetMapping("/me")
    public ResponseEntity<Merchant> getCurrentMerchant(Authentication authentication) {
        Merchant merchant = (Merchant) authentication.getPrincipal();
        // Refresh from DB to get latest
        return ResponseEntity.ok(merchantRepository.findById(merchant.getId()).orElse(merchant));
    }

    @PostMapping("/me/webhook/secret")
    public ResponseEntity<Map<String, String>> regenerateWebhookSecret(Authentication authentication) {
        Merchant merchant = (Merchant) authentication.getPrincipal();
        // Refresh
        merchant = merchantRepository.findById(merchant.getId()).orElse(merchant);

        String newSecret = "whsec_" + generateRandomString(32);
        merchant.setWebhookSecret(newSecret);
        merchantRepository.save(merchant);

        return ResponseEntity.ok(java.util.Collections.singletonMap("webhook_secret", newSecret));
    }

    @PutMapping("/me/webhook")
    public ResponseEntity<Merchant> updateWebhookUrl(
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        Merchant merchant = (Merchant) authentication.getPrincipal();
        merchant = merchantRepository.findById(merchant.getId()).orElse(merchant);

        String url = payload.get("webhook_url");
        if (url != null) {
            merchant.setWebhookUrl(url);
        }

        return ResponseEntity.ok(merchantRepository.save(merchant));
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(SECURE_RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
