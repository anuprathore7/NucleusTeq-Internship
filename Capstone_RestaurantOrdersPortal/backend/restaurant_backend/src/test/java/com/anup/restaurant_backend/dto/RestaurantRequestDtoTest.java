package com.anup.restaurant_backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests RestaurantRequestDto.
 */
class RestaurantRequestDtoTest {

    @Test
    void testSetGet_positive() {
        RestaurantRequestDto dto = new RestaurantRequestDto();
        dto.setName("Cafe");

        assertEquals("Cafe", dto.getName());
    }

    @Test
    void testNull_negative() {
        RestaurantRequestDto dto = new RestaurantRequestDto();
        assertNull(dto.getName());
    }
}