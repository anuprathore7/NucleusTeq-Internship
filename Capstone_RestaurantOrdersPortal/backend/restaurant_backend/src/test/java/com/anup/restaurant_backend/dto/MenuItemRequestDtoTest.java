package com.anup.restaurant_backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests MenuItemRequestDto.
 */
class MenuItemRequestDtoTest {

    @Test
    void testValid_positive() {
        MenuItemRequestDto dto = new MenuItemRequestDto();
        dto.setName("Pizza");
        dto.setPrice(200.0);

        assertEquals("Pizza", dto.getName());
        assertEquals(200.0, dto.getPrice());
    }

    @Test
    void testNull_negative() {
        MenuItemRequestDto dto = new MenuItemRequestDto();
        assertNull(dto.getName());
    }
}