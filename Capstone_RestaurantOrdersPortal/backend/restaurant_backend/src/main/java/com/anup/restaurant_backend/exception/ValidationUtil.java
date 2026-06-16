package com.anup.restaurant_backend.exception;

/**
 *   ValidationUtil
 *
 */
public class ValidationUtil {

    /**
     * Field must not be null or empty
     * Example: requireNotBlank("", "Email") → throws "Email is required"
     */
    public static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required");
        }
    }

    /**
     * String must be at least minLength characters
     * Example: requireMinLength("abc", 6, "Password") → throws "Password must be at least 6 characters"
     */
    public static void requireMinLength(String value, int minLength, String fieldName) {
        if (value != null && value.trim().length() < minLength) {
            throw new ValidationException(
                    fieldName + " must be at least " + minLength + " characters"
            );
        }
    }

    /**
     * Basic email format check
     * Example: requireValidEmail("notanemail") → throws "Please enter a valid email address"
     */
    public static void requireValidEmail(String email) {
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ValidationException("Please enter a valid email address");
        }
    }

    /**
     * Phone must be 10 digits
     * Example: requireValidPhone("123") → throws "Phone number must be 10 digits"
     */
    public static void requireValidPhone(String phone) {
        if (phone == null || !phone.matches("^[0-9]{10}$")) {
            throw new ValidationException("Phone number must be 10 digits");
        }
    }

    /**
     * Number must be positive
     * Example: requirePositive(-5.0, "Price") → throws "Price must be greater than 0"
     */
    public static void requirePositive(Double value, String fieldName) {
        if (value == null || value <= 0) {
            throw new ValidationException(fieldName + " must be greater than 0");
        }
    }

    /**
     * Value must not be null
     */
    public static void requireNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " is required");
        }
    }

    /**
     * Quantity must be at least 1
     */
    public static void requireValidQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new ValidationException("Quantity must be at least 1");
        }
    }
}