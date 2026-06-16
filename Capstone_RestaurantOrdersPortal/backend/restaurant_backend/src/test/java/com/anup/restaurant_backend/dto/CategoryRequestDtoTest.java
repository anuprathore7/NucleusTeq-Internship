package com.anup.restaurant_backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests CategoryRequestDto.
 */
class CategoryRequestDtoTest {

    @Test
    void testSetGet_positive() {
        CategoryRequestDto dto = new CategoryRequestDto();
        dto.setName("Starters");

        assertEquals("Starters", dto.getName());
    }

    @Test
    void testNull_negative() {
        CategoryRequestDto dto = new CategoryRequestDto();
        assertNull(dto.getName());
    }
}