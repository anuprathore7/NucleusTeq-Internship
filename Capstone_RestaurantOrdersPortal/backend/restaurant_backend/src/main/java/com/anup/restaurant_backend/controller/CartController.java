
/**
 * ============================================
 *   CartController
 * ============================================
 *
 *
 *
 * This will bw this flow :
 *
 * First login as CUSTOMER (not owner):
 * POST /api/auth/login
 * Body: { "email": "customer@test.com", "password": "1234" }
 * → copy token
 *
 * 1. ADD ITEM:
 *    POST http://localhost:8080/api/cart/add
 *    Header: Authorization: Bearer <token>
 *    Body: { "menuItemId": 3, "quantity": 1 }
 *    → Response: full cart with items and total
 *
 * 2. ADD ANOTHER ITEM (same restaurant):
 *    POST http://localhost:8080/api/cart/add
 *    Header: Authorization: Bearer <token>
 *    Body: { "menuItemId": 1, "quantity": 2 }
 *
 * 3. TRY ADDING FROM DIFFERENT RESTAURANT (should fail):
 *    POST http://localhost:8080/api/cart/add
 *    Body: { "menuItemId": 99, "quantity": 1 }  ← item from different restaurant
 *    → Error: "Your cart has items from Pizza Hub. Clear cart first"
 *
 * 4. VIEW CART:
 *    GET http://localhost:8080/api/cart
 *    Header: Authorization: Bearer <token>
 *
 * 5. UPDATE QUANTITY:
 *    PUT http://localhost:8080/api/cart/update/1
 *    Header: Authorization: Bearer <token>
 *    Body: { "quantity": 3 }
 *
 * 6. REMOVE ONE ITEM:
 *    DELETE http://localhost:8080/api/cart/remove/1
 *    Header: Authorization: Bearer <token>
 *
 * 7. CLEAR CART:
 *    DELETE http://localhost:8080/api/cart/clear
 *    Header: Authorization: Bearer <token>
**/

package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.CartItemRequestDto;
import com.anup.restaurant_backend.dto.CartResponseDto;
import com.anup.restaurant_backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CartController - Updated
 *
 * KEY FIX: getCart now returns 200 with empty cart
 * instead of throwing 404 when cart is empty.
 * Frontend needs this to show "cart is empty" state.
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public CartResponseDto addItem(
            @RequestBody CartItemRequestDto request,
            @RequestHeader("Authorization") String token) {
        return cartService.addItem(request, token);
    }

    /**
     * GET CART — returns empty cart response if no cart exists
     * Frontend checks: if items.length === 0 → show empty state
     */
    @GetMapping
    public ResponseEntity<?> getCart(
            @RequestHeader("Authorization") String token) {
        try {
            CartResponseDto cart = cartService.getCart(token);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            // Cart doesn't exist → return empty response (not 404)
            return ResponseEntity.ok(new CartResponseDto(null, null, null, java.util.List.of(), 0.0));
        }
    }

    @PutMapping("/update/{cartItemId}")
    public CartResponseDto updateItem(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity,
            @RequestHeader("Authorization") String token) {
        return cartService.updateItem(cartItemId, quantity, token);
    }

    @DeleteMapping("/remove/{cartItemId}")
    public CartResponseDto removeItem(
            @PathVariable Long cartItemId,
            @RequestHeader("Authorization") String token) {
        return cartService.removeItem(cartItemId, token);
    }

    @DeleteMapping("/clear")
    public String clearCart(
            @RequestHeader("Authorization") String token) {
        cartService.clearCart(token);
        return "Cart cleared successfully";
    }
}