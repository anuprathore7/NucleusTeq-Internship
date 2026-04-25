package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.CartItemRequestDto;
import com.anup.restaurant_backend.dto.CartResponseDto;

/**
 * ============================================
 *   CartService Interface
 * ============================================
 *
 *  ALL CART OPERATIONS:
 * 1. addItem    → add food item to cart
 * 2. getCart    → view my current cart
 * 3. updateItem → change quantity of an item
 * 4. removeItem → remove one item from cart
 * 5. clearCart  → empty the entire cart
 *
 *  ALL methods take token → userId extracted inside service
 * No userId ever comes from URL or body (security!)
 */
public interface CartService {

    /**
     * Add item to cart
     * If cart doesn't exist → create it
     * If item already in cart → increase quantity
     * If different restaurant → throw error
     */
    CartResponseDto addItem(CartItemRequestDto request, String token);

    /**
     * View my full cart with all items and total
     */
    CartResponseDto getCart(String token);

    /**
     * Change quantity of a specific cart item
     * @param cartItemId which item to update
     * @param quantity   new quantity
     */
    CartResponseDto updateItem(Long cartItemId, Integer quantity, String token);

    /**
     * Remove one specific item from cart
     * @param cartItemId which item to remove
     */
    CartResponseDto removeItem(Long cartItemId, String token);

    /**
     * Clear entire cart (used before placing order too)
     */
    void clearCart(String token);
}