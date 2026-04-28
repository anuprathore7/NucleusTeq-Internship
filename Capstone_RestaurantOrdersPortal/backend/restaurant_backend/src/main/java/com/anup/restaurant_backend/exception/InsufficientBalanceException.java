package com.anup.restaurant_backend.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(Double required, Double available) {
        super("Insufficient wallet balance. Required: ₹" +
                String.format("%.2f", required) +
                ", Available: ₹" +
                String.format("%.2f", available));
    }
}
