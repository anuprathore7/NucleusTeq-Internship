package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.OrderResponseDto;

import java.util.List;

/**
 * Service interface for all order operations.
 */
public interface OrderService {

    /**
     * Places an order from the user's cart to the given delivery address.
     * POST /api/orders/place
     */
    OrderResponseDto placeOrder(String token, Long deliveryAddressId);

    /**
     * Returns all orders placed by the authenticated customer.
     * GET /api/orders/my-orders
     */
    List<OrderResponseDto> getMyOrders(String token);

    /**
     * Cancels a PLACED order within 30 seconds and refunds the wallet.
     * DELETE /api/orders/cancel/{orderId}
     */
    OrderResponseDto cancelOrder(Long orderId, String token);

    /**
     * Returns all orders for a restaurant owned by the authenticated user.
     * GET /api/orders/restaurant/{restaurantId}
     */
    List<OrderResponseDto> getRestaurantOrders(Long restaurantId, String token);

    /**
     * Advances an order to the next status in the lifecycle.
     * PATCH /api/orders/status/{orderId}
     */
    OrderResponseDto updateOrderStatus(Long orderId, String status, String token);
}