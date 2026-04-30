package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.CartItemRequestDto;
import com.anup.restaurant_backend.dto.CartResponseDto;
import com.anup.restaurant_backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing the authenticated user's shopping cart.
 * All endpoints require a valid JWT token in the Authorization header.
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Adds a menu item to the cart or increases its quantity if already present.
     */
    @PostMapping("/add")
    public CartResponseDto addItem(
            @RequestBody CartItemRequestDto request,
            @RequestHeader("Authorization") String token) {
        return cartService.addItem(request, token);
    }

    /**
     * Returns the current cart for the authenticated user.
     * Returns an empty cart response if no cart exists instead of a 404.
     */
    @GetMapping
    public ResponseEntity<?> getCart(
            @RequestHeader("Authorization") String token) {
        try {
            CartResponseDto cart = cartService.getCart(token);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.ok(new CartResponseDto(null, null, null, java.util.List.of(), 0.0));
        }
    }

    /**
     * Updates the quantity of a specific item already in the cart.
     */
    @PutMapping("/update/{cartItemId}")
    public CartResponseDto updateItem(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity,
            @RequestHeader("Authorization") String token) {
        return cartService.updateItem(cartItemId, quantity, token);
    }

    /**
     * Removes a single item from the cart by its cart item ID.
     */
    @DeleteMapping("/remove/{cartItemId}")
    public CartResponseDto removeItem(
            @PathVariable Long cartItemId,
            @RequestHeader("Authorization") String token) {
        return cartService.removeItem(cartItemId, token);
    }

    /**
     * Clears all items from the authenticated user's cart.
     */
    @DeleteMapping("/clear")
    public String clearCart(
            @RequestHeader("Authorization") String token) {
        cartService.clearCart(token);
        return "Cart cleared successfully";
    }
}