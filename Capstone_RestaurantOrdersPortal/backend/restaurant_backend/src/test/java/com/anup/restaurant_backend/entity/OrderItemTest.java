package com.anup.restaurant_backend.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests OrderItem entity representing items in an order.
 */
public class OrderItemTest {

    /**
     * Ensures item name, quantity and price are stored correctly.
     */
    @Test
    void testOrderItemFields() {
        OrderItem item = new OrderItem();

        item.setId(1L);
        item.setItemName("Pizza");
        item.setQuantity(2);
        item.setPrice(300.0);

        assertEquals("Pizza", item.getItemName());
        assertEquals(2, item.getQuantity());
        assertEquals(300.0, item.getPrice());
    }
}