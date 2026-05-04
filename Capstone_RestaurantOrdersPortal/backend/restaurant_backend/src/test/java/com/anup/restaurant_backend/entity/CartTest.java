package com.anup.restaurant_backend.entity;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Cart entity behavior.
 * Ensures cart properties like total amount and items work properly.
 */
public class CartTest {

    /**
     * Checks basic cart field assignment.
     */
    @Test
    void testCartFields() {
        Cart cart = new Cart();

        cart.setId(10L);
        cart.setTotalAmount(999.0);
        cart.setItems(new ArrayList<>());

        assertEquals(10L, cart.getId());
        assertEquals(999.0, cart.getTotalAmount());
        assertNotNull(cart.getItems());
    }
}