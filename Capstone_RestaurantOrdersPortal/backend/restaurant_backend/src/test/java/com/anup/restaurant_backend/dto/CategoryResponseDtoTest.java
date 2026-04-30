package com.anup.restaurant_backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests CategoryResponseDto.
 */
class CategoryResponseDtoTest {

    @Test
    void testConstructor_positive() {
        CategoryResponseDto dto = new CategoryResponseDto(1L,"Starters",5L);

        assertEquals("Starters", dto.getName());
    }

    @Test
    void testDefault_negative() {
        CategoryResponseDto dto = new CategoryResponseDto();
        assertNull(dto.getId());
    }
}