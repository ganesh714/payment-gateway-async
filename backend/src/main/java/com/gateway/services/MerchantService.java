package com.gateway.services;

import com.gateway.models.Merchant;
import com.gateway.repositories.MerchantRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MerchantService {

    @Autowired
    private MerchantRepository merchantRepository;

    @Value("${gateway.test.merchant.id}")
    private String testMerchantId;

    @Value("${gateway.test.merchant.name}")
    private String testMerchantName;

    @Value("${gateway.test.merchant.email}")
    private String testMerchantEmail;

    @Value("${gateway.test.merchant.api-key}")
    private String testMerchantApiKey;

    @Value("${gateway.test.merchant.api-secret}")
    private String testMerchantApiSecret;

    @PostConstruct
    public void seedTestMerchant() {
        if (merchantRepository.findByEmail(testMerchantEmail).isPresent()) {
            return;
        }

        Merchant merchant = new Merchant();
        merchant.setId(UUID.fromString(testMerchantId));
        merchant.setName(testMerchantName);
        merchant.setEmail(testMerchantEmail);
        merchant.setApiKey(testMerchantApiKey);
        merchant.setApiSecret(testMerchantApiSecret);

        merchantRepository.save(merchant);
        System.out.println("Test merchant seeded successfully with ID: " + merchant.getId());
    }

    public Merchant getTestMerchant() {
        return merchantRepository.findByEmail(testMerchantEmail).orElse(null);
    }
}
