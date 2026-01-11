package com.gateway.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");

        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(1)) {
                response.put("database", "connected");
            } else {
                response.put("database", "disconnected");
            }
        } catch (Exception e) {
            response.put("database", "disconnected");
        }

        response.put("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now()));

        return ResponseEntity.ok(response);
    }
}
