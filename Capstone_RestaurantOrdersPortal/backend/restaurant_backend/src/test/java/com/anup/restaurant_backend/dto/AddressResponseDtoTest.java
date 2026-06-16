package com.anup.restaurant_backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for AddressResponseDto.
 * Validates constructor logic and full address formatting.
 */
class AddressResponseDtoTest {

    /**
     * Tests full constructor and derived fullAddress field.
     */
    @Test
    void testConstructor_positive() {
        AddressResponseDto dto = new AddressResponseDto(
                1L, "Street 1", "Delhi", "Delhi", "110001"
        );

        assertEquals(1L, dto.getId());
        assertEquals("Street 1", dto.getStreet());
        assertEquals("Delhi", dto.getCity());
        assertEquals("Delhi", dto.getState());
        assertEquals("110001", dto.getPincode());
        assertEquals("Street 1, Delhi, Delhi - 110001", dto.getFullAddress());
    }

    /**
     * Tests default constructor behavior.
     */
    @Test
    void testDefaultConstructor_negative() {
        AddressResponseDto dto = new AddressResponseDto();

        assertNull(dto.getId());
        assertNull(dto.getStreet());
        assertNull(dto.getFullAddress());
    }
}