package com.anup.restaurant_backend.exception;

public class OrderCancellationException extends RuntimeException {
    public OrderCancellationException() {
        super("Cancellation window has expired. Orders can only be cancelled within 30 seconds of placing.");
    }
}