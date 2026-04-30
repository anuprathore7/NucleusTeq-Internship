package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.OrderResponseDto;
import com.anup.restaurant_backend.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for all order-related operations.
 * Handles placing, viewing, cancelling, and status updates for orders.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Places a new order from the authenticated user's cart.
     * Expects a JSON body with deliveryAddressId.
     * POST /api/orders/place
     */
    @PostMapping("/place")
    public OrderResponseDto placeOrder(
            @RequestBody Map<String, Long> body,
            @RequestHeader("Authorization") String token) {
        Long addressId = body.get("deliveryAddressId");
        return orderService.placeOrder(token, addressId);
    }

    /**
     * Returns all orders of the authenticated customer, newest first.
     * GET /api/orders/my-orders
     */
    @GetMapping("/my-orders")
    public List<OrderResponseDto> getMyOrders(
            @RequestHeader("Authorization") String token) {
        return orderService.getMyOrders(token);
    }

    /**
     * Cancels an order within the 30-second window if still in PLACED status.
     * DELETE /api/orders/cancel/{orderId}
     */
    @DeleteMapping("/cancel/{orderId}")
    public OrderResponseDto cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String token) {
        return orderService.cancelOrder(orderId, token);
    }

    /**
     * Returns all orders for a restaurant owned by the authenticated user.
     * GET /api/orders/restaurant/{restaurantId}
     */
    @GetMapping("/restaurant/{restaurantId}")
    public List<OrderResponseDto> getRestaurantOrders(
            @PathVariable Long restaurantId,
            @RequestHeader("Authorization") String token) {
        return orderService.getRestaurantOrders(restaurantId, token);
    }

    /**
     * Updates the status of an order following the defined lifecycle.
     * PATCH /api/orders/status/{orderId}?status=ACCEPTED
     */
    @PatchMapping("/status/{orderId}")
    public OrderResponseDto updateStatus(
            @PathVariable Long orderId,
            @RequestParam String status,
            @RequestHeader("Authorization") String token) {
        return orderService.updateOrderStatus(orderId, status, token);
    }
}