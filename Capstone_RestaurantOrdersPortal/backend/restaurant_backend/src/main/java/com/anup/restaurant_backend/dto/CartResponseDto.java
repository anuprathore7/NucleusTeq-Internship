package com.anup.restaurant_backend.dto;

import java.util.List;

/**
 * Represents the full cart response returned to the user.
 * It includes restaurant details, all items in cart, and total amount.
 */
public class CartResponseDto {

    private Long cartId;
    private Long restaurantId;
    private String restaurantName;
    private List<CartItemResponseDto> items;
    private Double totalAmount;

    /**
     * Default constructor.
     */
    public CartResponseDto() {}

    /**
     * Creates a complete cart response with all details.
     */
    public CartResponseDto(Long cartId, Long restaurantId, String restaurantName,
                           List<CartItemResponseDto> items, Double totalAmount) {
        this.cartId = cartId;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    /**
     * Returns cart ID.
     */
    public Long getCartId() { return cartId; }

    /**
     * Returns restaurant ID.
     */
    public Long getRestaurantId() { return restaurantId; }

    /**
     * Returns restaurant name.
     */
    public String getRestaurantName() { return restaurantName; }

    /**
     * Returns list of cart items.
     */
    public List<CartItemResponseDto> getItems() { return items; }

    /**
     * Returns total amount of the cart.
     */
    public Double getTotalAmount() { return totalAmount; }
}