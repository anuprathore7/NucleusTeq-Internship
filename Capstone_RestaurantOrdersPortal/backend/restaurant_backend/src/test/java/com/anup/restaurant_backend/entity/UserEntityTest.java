package com.anup.restaurant_backend.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests UserEntity which represents application users.
 */
public class UserEntityTest {

    /**
     * Verifies user fields like name, email and wallet.
     */
    @Test
    void testUserFields() {
        UserEntity user = new UserEntity();

        user.setId(1L);
        user.setFirstName("Anup");
        user.setEmail("test@gmail.com");
        user.setWalletBalance(1000.0);

        assertEquals("Anup", user.getFirstName());
        assertEquals("test@gmail.com", user.getEmail());
        assertEquals(1000.0, user.getWalletBalance());
    }
}