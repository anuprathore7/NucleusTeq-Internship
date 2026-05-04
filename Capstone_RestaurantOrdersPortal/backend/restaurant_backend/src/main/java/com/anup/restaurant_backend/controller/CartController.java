package com.anup.restaurant_backend.controller;
import com.anup.restaurant_backend.dto.CartItemRequestDto;
import com.anup.restaurant_backend.dto.CartResponseDto;
import com.anup.restaurant_backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * This controller manages all cart-related operations for a user.
 * It allows adding items, updating quantity, removing items,
 * viewing the cart, and clearing it completely.
 */
@RestController
@RequestMapping(CartController.BASE_URL)
public class CartController {
    public static final String BASE_URL="/api/cart";
    public static final String ADD="/add";
    public static final String UPDATE="/update/{cartItemId}";
    public static final String REMOVE="/remove/{cartItemId}";
    public static final String CLEAR="/clear";
    private final CartService cartService;
    public CartController(CartService cartService){this.cartService=cartService;}
    /**
     * Adds an item to the cart or increases its quantity if already present.
     */
    @PostMapping(ADD)
    public CartResponseDto addItem(@RequestBody CartItemRequestDto request,@RequestHeader("Authorization") String token){
        return cartService.addItem(request,token);
    }
    /**
     * Fetches the current cart of the user.
     * Returns an empty cart if none exists.
     */
    @GetMapping
    public ResponseEntity<?> getCart(@RequestHeader("Authorization") String token){
        try{
            CartResponseDto cart=cartService.getCart(token);
            return ResponseEntity.ok(cart);
        }catch(Exception e){
            return ResponseEntity.ok(new CartResponseDto(null,null,null,java.util.List.of(),0.0));
        }
    }
    /**
     * Updates the quantity of a specific item in the cart.
     */
    @PutMapping(UPDATE)
    public CartResponseDto updateItem(@PathVariable Long cartItemId,@RequestParam Integer quantity,@RequestHeader("Authorization") String token){
        return cartService.updateItem(cartItemId,quantity,token);
    }
    /**
     * Removes a single item from the cart.
     */
    @DeleteMapping(REMOVE)
    public CartResponseDto removeItem(@PathVariable Long cartItemId,@RequestHeader("Authorization") String token){
        return cartService.removeItem(cartItemId,token);
    }
    /**
     * Clears all items from the user's cart.
     */
    @DeleteMapping(CLEAR)
    public String clearCart(@RequestHeader("Authorization") String token){
        cartService.clearCart(token);
        return "Cart cleared successfully";
    }
}