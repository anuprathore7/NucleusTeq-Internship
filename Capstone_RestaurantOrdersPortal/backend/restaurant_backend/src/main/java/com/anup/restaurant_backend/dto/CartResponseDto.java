package com.anup.restaurant_backend.dto;

import java.util.List;

/**
 * ============================================
 *   CartResponseDto
 * ============================================
 */
public class CartResponseDto {

    private Long cartId;
    private Long restaurantId;
    private String restaurantName;
    private List<CartItemResponseDto> items;
    private Double totalAmount;

    public CartResponseDto() {}

    public CartResponseDto(Long cartId, Long restaurantId, String restaurantName,
                           List<CartItemResponseDto> items, Double totalAmount) {
        this.cartId = cartId;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    public Long getCartId() { return cartId; }
    public Long getRestaurantId() { return restaurantId; }
    public String getRestaurantName() { return restaurantName; }
    public List<CartItemResponseDto> getItems() { return items; }
    public Double getTotalAmount() { return totalAmount; }
}