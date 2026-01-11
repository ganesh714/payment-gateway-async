package com.gateway.services;

import com.gateway.models.Merchant;
import com.gateway.models.Order;
import com.gateway.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private OrderRepository orderRepository;

    public Order createOrder(Merchant merchant, Integer amount, String currency, String receipt,
            Map<String, Object> notes) {
        if (amount < 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be at least 100");
        }

        Order order = new Order();
        order.setId(generateOrderId());
        order.setMerchantId(merchant.getId());
        order.setAmount(amount);
        order.setCurrency(currency != null ? currency : "INR");
        order.setReceipt(receipt);
        order.setNotes(notes);
        order.setStatus("created");

        return orderRepository.save(order);
    }

    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private String generateOrderId() {
        StringBuilder sb = new StringBuilder("order_");
        for (int i = 0; i < 16; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
