package com.anup.restaurant_backend.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Order entity which represents a placed order.
 */
public class OrderTest {

    /**
     * Checks total amount and timestamp behavior.
     */
    @Test
    void testOrderFields() {
        Order order = new Order();

        order.setId(1L);
        order.setTotalAmount(500.0);
        order.setCreatedAt(LocalDateTime.now());

        assertEquals(1L, order.getId());
        assertEquals(500.0, order.getTotalAmount());
        assertNotNull(order.getCreatedAt());
    }
}