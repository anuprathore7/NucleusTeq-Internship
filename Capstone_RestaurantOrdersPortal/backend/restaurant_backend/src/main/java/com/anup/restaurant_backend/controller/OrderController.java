package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.OrderResponseDto;
import com.anup.restaurant_backend.service.OrderService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * ============================================
 *   OrderController
 * ============================================
 *
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * PLACE ORDER
     * POST /api/orders/place
     * Customer only — reads cart from JWT user
     */
    @PostMapping("/place")
    public OrderResponseDto placeOrder(
            @RequestHeader("Authorization") String token) {
        return orderService.placeOrder(token);
    }

    /**
     * MY ORDER HISTORY
     * GET /api/orders/my-orders
     */
    @GetMapping("/my-orders")
    public List<OrderResponseDto> getMyOrders(
            @RequestHeader("Authorization") String token) {
        return orderService.getMyOrders(token);
    }

    /**
     * CANCEL ORDER (within 30 seconds)
     * DELETE /api/orders/cancel/{orderId}
     */
    @DeleteMapping("/cancel/{orderId}")
    public OrderResponseDto cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader("Authorization") String token) {
        return orderService.cancelOrder(orderId, token);
    }

    /**
     * VIEW ORDERS FOR A RESTAURANT (owner)
     * GET /api/orders/restaurant/{restaurantId}
     */
    @GetMapping("/restaurant/{restaurantId}")
    public List<OrderResponseDto> getRestaurantOrders(
            @PathVariable Long restaurantId,
            @RequestHeader("Authorization") String token) {
        return orderService.getRestaurantOrders(restaurantId, token);
    }

    /**
     * UPDATE ORDER STATUS (owner)
     * PATCH /api/orders/status/{orderId}?status=PENDING
     */
    @PatchMapping("/status/{orderId}")
    public OrderResponseDto updateStatus(
            @PathVariable Long orderId,
            @RequestParam String status,
            @RequestHeader("Authorization") String token) {
        return orderService.updateOrderStatus(orderId, status, token);
    }
}