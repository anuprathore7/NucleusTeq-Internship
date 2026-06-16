package com.anup.restaurant_backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests UserRequestDto.
 */
class UserRequestDtoTest {

    @Test
    void testSetGet_positive() {
        UserRequestDto dto = new UserRequestDto();
        dto.setEmail("test@mail.com");

        assertEquals("test@mail.com", dto.getEmail());
    }

    @Test
    void testNull_negative() {
        UserRequestDto dto = new UserRequestDto();
        assertNull(dto.getEmail());
    }
}