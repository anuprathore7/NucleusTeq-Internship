package com.anup.restaurant_backend.dto;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests CartResponseDto which represents full cart.
 */
class CartResponseDtoTest {

    /**
     * Tests constructor with items.
     */
    @Test
    void testConstructor_positive() {
        CartResponseDto dto = new CartResponseDto(1L,2L,"Dominos",List.of(),500.0);

        assertEquals("Dominos", dto.getRestaurantName());
        assertEquals(500.0, dto.getTotalAmount());
    }

    /**
     * Tests empty state.
     */
    @Test
    void testDefault_negative() {
        CartResponseDto dto = new CartResponseDto();

        assertNull(dto.getItems());
    }
}