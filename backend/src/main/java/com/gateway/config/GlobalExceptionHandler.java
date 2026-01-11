package com.gateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        String code = "BAD_REQUEST_ERROR";
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            code = "NOT_FOUND_ERROR";
        } else if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            code = "AUTHENTICATION_ERROR";
        }

        return buildErrorResponse(ex.getStatusCode().value(), code, ex.getReason());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        // Log the full exception internally
        System.err.println("CRITICAL: Unhandled exception caught: " + ex.getMessage());
        ex.printStackTrace();

        // Return a safe, generic error response to the client
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please contact support.");
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(int status, String code, String description) {
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("description", description);

        Map<String, Object> body = new HashMap<>();
        body.put("error", error);

        return ResponseEntity.status(status).body(body);
    }
}
