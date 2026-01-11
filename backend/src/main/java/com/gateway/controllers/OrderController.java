package com.gateway.controllers;

import com.gateway.models.Merchant;
import com.gateway.models.Order;
import com.gateway.repositories.MerchantRepository;
import com.gateway.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MerchantRepository merchantRepository;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Map<String, Object> payload, Authentication authentication) {
        Merchant merchant = (Merchant) authentication.getPrincipal();

        Integer amount = (Integer) payload.get("amount");
        String currency = (String) payload.getOrDefault("currency", "INR");
        String receipt = (String) payload.get("receipt");
        Map<String, Object> notes = (Map<String, Object>) payload.get("notes");

        if (amount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount is required");
        }

        Order order = orderService.createOrder(merchant, amount, currency, receipt, notes);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId, Authentication authentication) {
        Merchant merchant = (Merchant) authentication.getPrincipal();
        Order order = orderService.getOrder(orderId);

        if (!order.getMerchantId().equals(merchant.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"); // Don't leak existence
        }

        return ResponseEntity.ok(order);
    }

    @GetMapping("/{orderId}/public")
    public ResponseEntity<Order> getOrderPublic(@PathVariable String orderId) {
        Order order = orderService.getOrder(orderId);
        // Public endpoint allowed to see basic order details for checkout
        return ResponseEntity.ok(order);
    }
}
