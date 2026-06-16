package com.anup.restaurant_backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * GlobalExceptionHandler
 *
 * Catches ALL exceptions thrown anywhere in the app
 * and returns clean, user-friendly JSON messages.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**  404 Not Found  */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        log.error("Not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** 400 Validation / Business Logic */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(ValidationException ex) {
        log.warn("Validation: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** 403 Forbidden */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {
        log.warn("Unauthorized: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /** 400 Insufficient Wallet */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleWallet(InsufficientBalanceException ex) {
        log.warn("Wallet: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** 400 Order Cancellation Time Expired */
    @ExceptionHandler(OrderCancellationException.class)
    public ResponseEntity<Map<String, Object>> handleCancellation(OrderCancellationException ex) {
        log.warn("Cancellation: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** 400 Wrong URL param type (e.g. /api/cart/add/abc) */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, "Invalid value for parameter: " + ex.getName());
    }

    /** Catch-all RuntimeException */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        log.error("Runtime error: {}", ex.getMessage());
        String msg = ex.getMessage() != null ? ex.getMessage() : "Something went wrong";

        if (msg.toLowerCase().contains("not authorized") ||
                msg.toLowerCase().contains("unauthorized") ||
                msg.toLowerCase().contains("not allowed")) {
            return build(HttpStatus.FORBIDDEN, msg);
        }
        if (msg.toLowerCase().contains("not found")) {
            return build(HttpStatus.NOT_FOUND, msg);
        }
        if (msg.toLowerCase().contains("already exists") ||
                msg.toLowerCase().contains("already registered")) {
            return build(HttpStatus.CONFLICT, msg);
        }
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    /** 500 Unexpected errors */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong on our end. Please try again.");
    }

    /** Helper: builds { "message": "..." } only */
    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message); // ONLY message — clean for UI
        return new ResponseEntity<>(body, status);
    }

    /**
     * Handles missing request headers like Authorization.
     * Returns 400 Bad Request instead of 500.
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<String> handleMissingHeader(MissingRequestHeaderException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }
}