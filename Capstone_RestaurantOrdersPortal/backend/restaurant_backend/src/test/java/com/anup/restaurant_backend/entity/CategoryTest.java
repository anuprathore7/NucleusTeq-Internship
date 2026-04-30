package com.anup.restaurant_backend.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Category entity which groups menu items.
 */
public class CategoryTest {

    /**
     * Ensures category name is stored correctly.
     */
    @Test
    void testCategoryFields() {
        Category category = new Category();

        category.setId(5L);
        category.setName("Pizza");

        assertEquals(5L, category.getId());
        assertEquals("Pizza", category.getName());
    }
}