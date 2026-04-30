package com.anup.restaurant_backend.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests MenuItem entity representing food items.
 */
public class MenuItemTest {

    /**
     * Checks item details like name, price, availability.
     */
    @Test
    void testMenuItemFields() {
        MenuItem item = new MenuItem();

        item.setId(1L);
        item.setName("Burger");
        item.setPrice(150.0);
        item.setAvailable(true);

        assertEquals(1L, item.getId());
        assertEquals("Burger", item.getName());
        assertEquals(150.0, item.getPrice());
        assertTrue(item.getAvailable());
    }
}