package com.anup.restaurant_backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests OrderItemResponseDto.
 */
class OrderItemResponseDtoTest {

    @Test
    void testConstructor_positive() {
        OrderItemResponseDto dto = new OrderItemResponseDto(1L,"Pizza",200.0,2,400.0);

        assertEquals(400.0, dto.getSubtotal());
    }

    @Test
    void testDefault_negative() {
        OrderItemResponseDto dto = new OrderItemResponseDto();
        assertNull(dto.getId());
    }
}