package com.anup.restaurant_backend.enums;

/**
 *
 * Enum = fixed set of constants
 *
 * Instead of writing role as String ("USER", "ADMIN"),
 * we define allowed values here.
 *
 * This prevents:
 * wrong spelling
 * invalid roles
 */

public enum UserRole {

    USER,
    RESTAURANT_OWNER

}