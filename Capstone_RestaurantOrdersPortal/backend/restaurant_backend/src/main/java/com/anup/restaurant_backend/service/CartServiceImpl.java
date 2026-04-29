package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.CartItemRequestDto;
import com.anup.restaurant_backend.dto.CartItemResponseDto;
import com.anup.restaurant_backend.dto.CartResponseDto;
import com.anup.restaurant_backend.entity.Cart;
import com.anup.restaurant_backend.entity.CartItem;
import com.anup.restaurant_backend.entity.MenuItem;
import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.exception.ResourceNotFoundException;
import com.anup.restaurant_backend.repository.CartItemRepository;
import com.anup.restaurant_backend.repository.CartRepository;
import com.anup.restaurant_backend.repository.MenuItemRepository;
import com.anup.restaurant_backend.repository.UserRepository;
import com.anup.restaurant_backend.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ============================================
 *   CartServiceImpl
 * ============================================
**/
@Service
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           MenuItemRepository menuItemRepository,
                           UserRepository userRepository,
                           JwtService jwtService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    // =====================================================
    //  METHOD 1: ADD ITEM TO CART
    // =====================================================

    @Override
    @Transactional
    public CartResponseDto addItem(CartItemRequestDto request, String token) {

        // Step 1: Get customer from JWT
        UserEntity customer = getUserFromToken(token);

        // Step 2: Find the menu item
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem not found with id: " + request.getMenuItemId()));

        // Check item is available
        if (!menuItem.getAvailable()) {
            throw new RuntimeException("This item is currently not available");
        }

        // Step 3: Get restaurant from menuItem automatically
        // Customer never sends restaurantId — item already knows it!
        var restaurant = menuItem.getRestaurant();

        // Step 4: Get or create cart
        Optional<Cart> existingCart = cartRepository.findByUserId(customer.getId());
        Cart cart;

        if (existingCart.isEmpty()) {
            // No cart exists → create new one for this restaurant
            log.info("Creating new cart for userId: {} at restaurantId: {}", customer.getId(), restaurant.getId());
            cart = new Cart();
            cart.setUser(customer);
            cart.setRestaurant(restaurant);
            cart = cartRepository.save(cart);

        } else {
            cart = existingCart.get();

            // Cart exists → check if same restaurant
            // THIS IS THE "ONE RESTAURANT AT A TIME" RULE
            if (!cart.getRestaurant().getId().equals(restaurant.getId())) {
                log.warn("userId: {} tried to add item from different restaurant", customer.getId());
                throw new RuntimeException(
                        "Your cart has items from " + cart.getRestaurant().getName() +
                                ". Clear your cart first to order from " + restaurant.getName()
                );
            }
        }

        // Step 5: Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndMenuItemId(cart.getId(), menuItem.getId());

        if (existingItem.isPresent()) {
            // Item already in cart → just increase quantity
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(cartItem);
            log.info("Increased quantity for menuItemId: {} in cartId: {}", menuItem.getId(), cart.getId());

        } else {
            // New item → create new CartItem row
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setMenuItem(menuItem);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPrice(menuItem.getPrice()); // Store price at this moment!
            cartItemRepository.save(cartItem);
            log.info("Added new item '{}' to cartId: {}", menuItem.getName(), cart.getId());
        }

        // Step 6: Return full updated cart
        // Step 6: Reload cart from DB so items list is fresh
        Cart freshCart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        return buildCartResponse(freshCart);
    }

    // =====================================================
    //  METHOD 2: GET CART
    // =====================================================

    /**
     *  Customer wants to VIEW their cart
     */
    @Override
    public CartResponseDto getCart(String token) {

        UserEntity customer = getUserFromToken(token);

        Cart cart = cartRepository.findByUserId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart is empty"));

        log.info("Fetching cart for userId: {}", customer.getId());

        return buildCartResponse(cart);
    }

    // =====================================================
    //  METHOD 3: UPDATE ITEM QUANTITY
    // =====================================================

    /**
     *  Customer changes quantity of an item
     * Example: had 1 pizza → wants 3 pizzas
     */
    @Override
    public CartResponseDto updateItem(Long cartItemId, Integer quantity, String token) {

        UserEntity customer = getUserFromToken(token);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        // Security check - this cart item must belong to this customer
        if (!cartItem.getCart().getUser().getId().equals(customer.getId())) {
            throw new RuntimeException("You are not authorized to update this cart item");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        log.info("Updated cartItemId: {} quantity to: {}", cartItemId, quantity);

        return buildCartResponse(cartItem.getCart());
    }

    // =====================================================
    //  METHOD 4: REMOVE ONE ITEM
    // =====================================================

    /**
     *  Customer removes ONE item from cart
     * Cart still exists with remaining items
     *
     * Security check same as update:
     * CartItem must belong to this customer's cart
     */
    @Override
    public CartResponseDto removeItem(Long cartItemId, String token) {

        UserEntity customer = getUserFromToken(token);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        // Security check
        if (!cartItem.getCart().getUser().getId().equals(customer.getId())) {
            throw new RuntimeException("You are not authorized to remove this cart item");
        }

        Cart cart = cartItem.getCart();
        cartItemRepository.deleteById(cartItemId);

        log.info("Removed cartItemId: {} from cartId: {}", cartItemId, cart.getId());

        // Refresh cart from DB after deletion
        cart = cartRepository.findById(cart.getId()).get();
        return buildCartResponse(cart);
    }

    // =====================================================
    //  METHOD 5: CLEAR ENTIRE CART
    // =====================================================

    /**
     *  Empties the entire cart
     * Used when:
     * → Customer clicks "Clear Cart" manually
     * → Order is placed successfully (called from OrderService)
     *
     * We delete the cart itself → cascade deletes all cart items too
     */
    @Override
    @Transactional
    public void clearCart(String token) {

        UserEntity customer = getUserFromToken(token);

        Cart cart = cartRepository.findByUserId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cartRepository.deleteById(cart.getId());

        log.info("Cart cleared for userId: {}", customer.getId());
    }

    // =====================================================
    //  PRIVATE HELPER 1: Get User From Token
    // =====================================================

    /**
     *  Reusable method to extract user from JWT
     * Same pattern as RestaurantServiceImpl
     * Used in every method above
     */
    private UserEntity getUserFromToken(String token) {
        String email = jwtService.extractEmail(token.substring(7));
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // =====================================================
    //  PRIVATE HELPER 2: Build Cart Response
    // =====================================================

    /**
     *  Converts Cart entity → CartResponseDto
     */
    private CartResponseDto buildCartResponse(Cart cart) {

        // ADD THIS NULL CHECK
        if (cart.getItems() == null) {
            cart.setItems(new java.util.ArrayList<>());
        }

        List<CartItemResponseDto> itemDtos = cart.getItems()
                .stream()
                .map(item -> new CartItemResponseDto(
                        item.getId(),
                        item.getMenuItem().getId(),
                        item.getMenuItem().getName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getPrice() * item.getQuantity()
                ))
                .collect(Collectors.toList());

        Double total = itemDtos.stream()
                .mapToDouble(CartItemResponseDto::getSubtotal)
                .sum();

        return new CartResponseDto(
                cart.getId(),
                cart.getRestaurant().getId(),
                cart.getRestaurant().getName(),
                itemDtos,
                total
        );
    }
}