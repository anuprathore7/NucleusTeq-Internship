package com.anup.restaurant_backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for AuthResponse DTO.
 * Verifies that token is correctly stored and returned.
 */
class AuthResponseTest {

    /**
     * Tests that constructor correctly assigns token.
     */
    @Test
    void testConstructor_positive() {
        AuthResponse response = new AuthResponse("token123");
        assertEquals("token123", response.getToken());
    }

    /**
     * Tests behavior when null token is provided.
     */
    @Test
    void testNullToken_negative() {
        AuthResponse response = new AuthResponse(null);
        assertNull(response.getToken());
    }
}