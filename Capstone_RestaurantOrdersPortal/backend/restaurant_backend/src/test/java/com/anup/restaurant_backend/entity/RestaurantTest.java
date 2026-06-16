package com.anup.restaurant_backend.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Restaurant entity.
 */
public class RestaurantTest {

    /**
     * Checks restaurant basic details.
     */
    @Test
    void testRestaurantFields() {
        Restaurant restaurant = new Restaurant();

        restaurant.setId(1L);
        restaurant.setName("KFC");

        assertEquals(1L, restaurant.getId());
        assertEquals("KFC", restaurant.getName());
    }
}