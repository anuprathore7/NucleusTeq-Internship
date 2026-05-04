package com.anup.restaurant_backend.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests basic behavior of Address entity.
 * Focuses on getters and setters since there is no business logic.
 */
public class AddressTest {

    /**
     * Verifies that all fields can be set and retrieved correctly.
     */
    @Test
    void testAddressFields() {
        Address address = new Address();

        address.setId(1L);
        address.setStreet("Street 1");
        address.setCity("Raipur");
        address.setState("CG");
        address.setPincode("492001");

        assertEquals(1L, address.getId());
        assertEquals("Street 1", address.getStreet());
        assertEquals("Raipur", address.getCity());
        assertEquals("CG", address.getState());
        assertEquals("492001", address.getPincode());
    }
}