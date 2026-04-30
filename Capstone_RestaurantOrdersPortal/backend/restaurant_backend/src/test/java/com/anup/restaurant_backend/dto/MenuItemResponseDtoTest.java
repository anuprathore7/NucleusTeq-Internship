package com.anup.restaurant_backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests MenuItemResponseDto.
 */
class MenuItemResponseDtoTest {

    @Test
    void testConstructor_positive() {
        MenuItemResponseDto dto = new MenuItemResponseDto(1L,"Pizza","desc",200.0,"img",true,2L,3L);

        assertEquals("Pizza", dto.getName());
    }

    @Test
    void testDefault_negative() {
        MenuItemResponseDto dto = new MenuItemResponseDto();
        assertNull(dto.getId());
    }
}