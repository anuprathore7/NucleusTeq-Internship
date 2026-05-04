package com.anup.restaurant_backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests CartItemRequestDto for adding items to cart.
 */
class CartItemRequestDtoTest {

    /**
     * Valid menu item and quantity.
     */
    @Test
    void testValid_positive() {
        CartItemRequestDto dto = new CartItemRequestDto();
        dto.setMenuItemId(10L);
        dto.setQuantity(2);

        assertEquals(10L, dto.getMenuItemId());
        assertEquals(2, dto.getQuantity());
    }

    /**
     * Default null values.
     */
    @Test
    void testNull_negative() {
        CartItemRequestDto dto = new CartItemRequestDto();

        assertNull(dto.getMenuItemId());
        assertNull(dto.getQuantity());
    }
}