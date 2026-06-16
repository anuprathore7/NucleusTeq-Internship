package com.anup.restaurant_backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests CartItemResponseDto for cart item details.
 */
class CartItemResponseDtoTest {

    /**
     * Tests constructor mapping.
     */
    @Test
    void testConstructor_positive() {
        CartItemResponseDto dto = new CartItemResponseDto(1L,2L,"Pizza",2,100.0,200.0);

        assertEquals("Pizza", dto.getMenuItemName());
        assertEquals(200.0, dto.getSubtotal());
    }

    /**
     * Default constructor behavior.
     */
    @Test
    void testDefault_negative() {
        CartItemResponseDto dto = new CartItemResponseDto();

        assertNull(dto.getMenuItemId());
    }
}