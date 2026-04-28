package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.OrderResponseDto;
import java.util.List;

/**
 * OrderService Interface
 */
public interface OrderService {

    // POST /api/orders/place
    OrderResponseDto placeOrder(String token);

    // GET /api/orders/my-orders
    List<OrderResponseDto> getMyOrders(String token);

    // DELETE /api/orders/cancel/{orderId}
    OrderResponseDto cancelOrder(Long orderId, String token);

    // GET /api/orders/restaurant/{restaurantId}
    List<OrderResponseDto> getRestaurantOrders(Long restaurantId, String token);

    // PATCH /api/orders/status/{orderId}
    OrderResponseDto updateOrderStatus(Long orderId, String status, String token);
}