package com.anup.restaurant_backend.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests CartItem entity which represents items inside cart.
 */
public class CartItemTest {

    /**
     * Verifies quantity and price storage.
     */
    @Test
    void testCartItemFields() {
        CartItem item = new CartItem();

        item.setId(1L);
        item.setQuantity(2);
        item.setPrice(200.0);

        assertEquals(1L, item.getId());
        assertEquals(2, item.getQuantity());
        assertEquals(200.0, item.getPrice());
    }
}