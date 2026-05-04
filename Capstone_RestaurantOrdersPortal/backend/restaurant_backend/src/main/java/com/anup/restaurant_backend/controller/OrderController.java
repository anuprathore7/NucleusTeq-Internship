package com.anup.restaurant_backend.controller;
import com.anup.restaurant_backend.dto.OrderResponseDto;
import com.anup.restaurant_backend.service.OrderService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * This controller handles all order-related actions in the system.
 * It allows users to place orders, view their order history,
 * cancel orders, and lets restaurant owners manage order status.
 */
@RestController
@RequestMapping(OrderController.BASE_URL)
public class OrderController {

    public static final String BASE_URL="/api/orders";
    public static final String PLACE="/place";
    public static final String MY_ORDERS="/my-orders";
    public static final String CANCEL="/cancel/{orderId}";
    public static final String RESTAURANT="/restaurant/{restaurantId}";
    public static final String STATUS="/status/{orderId}";
    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService=orderService;
    }
    /**
     * Places a new order using the user's cart and selected address.
     * Requires a valid token and address ID in request body.
     */
    @PostMapping(PLACE)
    public OrderResponseDto placeOrder(@RequestBody Map<String,Long> body,@RequestHeader("Authorization") String token){
        Long addressId=body.get("deliveryAddressId");
        return orderService.placeOrder(token,addressId);
    }
    /**
     * Returns all orders placed by the logged-in user.
     * Orders are usually returned in descending order of creation.
     */
    @GetMapping(MY_ORDERS)
    public List<OrderResponseDto> getMyOrders(@RequestHeader("Authorization") String token){
        return orderService.getMyOrders(token);
    }
    /**
     * Cancels an order if it is still eligible for cancellation.
     * Typically allowed only within a short time after placing.
     */
    @DeleteMapping(CANCEL)
    public OrderResponseDto cancelOrder(@PathVariable Long orderId,@RequestHeader("Authorization") String token){
        return orderService.cancelOrder(orderId,token);
    }
    /**
     * Returns all orders for a specific restaurant.
     * This is mainly used by the restaurant owner to manage orders.
     */
    @GetMapping(RESTAURANT)
    public List<OrderResponseDto> getRestaurantOrders(@PathVariable Long restaurantId,@RequestHeader("Authorization") String token){
        return orderService.getRestaurantOrders(restaurantId,token);
    }
    /**
     * Updates the status of an order such as ACCEPTED, DELIVERED, etc.
     * Only the restaurant owner is allowed to update order status.
     */
    @PatchMapping(STATUS)
    public OrderResponseDto updateStatus(@PathVariable Long orderId,@RequestParam String status,@RequestHeader("Authorization") String token){
        return orderService.updateOrderStatus(orderId,status,token);
    }
}