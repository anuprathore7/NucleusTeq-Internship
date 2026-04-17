    package com.anup.spring_advance_assignment.exception;

    public class InvalidStatusException extends RuntimeException {
        public InvalidStatusException(String message) {
            super(message);
        }
    }