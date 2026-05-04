package com.anup.restaurant_backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests RestaurantResponseDto.
 */
class RestaurantResponseDtoTest {

    @Test
    void testConstructor_positive() {
        RestaurantResponseDto dto = new RestaurantResponseDto(1L,"Cafe","desc","addr","123",2L,"img");

        assertEquals("Cafe", dto.getName());
    }

    @Test
    void testDefault_negative() {
        RestaurantResponseDto dto = new RestaurantResponseDto();
        assertNull(dto.getId());
    }
}