package com.anup.restaurant_backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================
 *   GlobalExceptionHandler
 * ============================================
 *
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 404 - Resource Not Found
     * Thrown when cart, order, restaurant, user not found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * 400 - Bad Request
     * Thrown for: insufficient wallet, cancellation expired,
     * empty cart, different restaurant, invalid status etc.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        log.error("Runtime error: {}", ex.getMessage());

        // Determine status code based on message content
        String msg = ex.getMessage() != null ? ex.getMessage() : "An error occurred";

        if (msg.contains("not authorized") || msg.contains("Unauthorized")) {
            return buildResponse(HttpStatus.FORBIDDEN, msg);
        }
        if (msg.contains("not found") || msg.contains("Not Found")) {
            return buildResponse(HttpStatus.NOT_FOUND, msg);
        }

        return buildResponse(HttpStatus.BAD_REQUEST, msg);
    }

    /**
     * 500 - Any other unexpected error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong. Please try again.");
    }

    /**
     * Helper to build consistent error response
     */
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());
        return new ResponseEntity<>(body, status);
    }
}