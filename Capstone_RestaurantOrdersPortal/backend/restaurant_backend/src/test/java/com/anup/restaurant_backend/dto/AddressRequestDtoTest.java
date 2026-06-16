package com.anup.restaurant_backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for AddressRequestDto.
 * Ensures setters and getters work as expected.
 */
class AddressRequestDtoTest {

    /**
     * Tests setting and retrieving valid address values.
     */
    @Test
    void testSettersGetters_positive() {
        AddressRequestDto dto = new AddressRequestDto();

        dto.setStreet("Street 1");
        dto.setCity("Delhi");
        dto.setState("Delhi");
        dto.setPincode("110001");

        assertEquals("Street 1", dto.getStreet());
        assertEquals("Delhi", dto.getCity());
        assertEquals("Delhi", dto.getState());
        assertEquals("110001", dto.getPincode());
    }

    /**
     * Tests default values when nothing is set.
     */
    @Test
    void testDefaultValues_negative() {
        AddressRequestDto dto = new AddressRequestDto();

        assertNull(dto.getStreet());
        assertNull(dto.getCity());
        assertNull(dto.getState());
        assertNull(dto.getPincode());
    }
}