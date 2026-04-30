package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.CartItemRequestDto;
import com.anup.restaurant_backend.dto.CartItemResponseDto;
import com.anup.restaurant_backend.dto.CartResponseDto;
import com.anup.restaurant_backend.entity.Cart;
import com.anup.restaurant_backend.entity.CartItem;
import com.anup.restaurant_backend.entity.MenuItem;
import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.exception.ResourceNotFoundException;
import com.anup.restaurant_backend.exception.ValidationException;
import com.anup.restaurant_backend.exception.UnauthorizedException;
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
    // ADD ITEM
    // =====================================================
    @Override
    @Transactional
    public CartResponseDto addItem(CartItemRequestDto request, String token) {

        UserEntity customer = getUserFromToken(token);

        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));


        if (!menuItem.getAvailable()) {
            throw new ValidationException("Sorry, this item is currently unavailable.");
        }

        var restaurant = menuItem.getRestaurant();

        Optional<Cart> existingCart = cartRepository.findByUserId(customer.getId());
        Cart cart;

        if (existingCart.isEmpty()) {
            cart = new Cart();
            cart.setUser(customer);
            cart.setRestaurant(restaurant);
            cart = cartRepository.save(cart);
        } else {
            cart = existingCart.get();


            // Allow switching restaurant if cart is empty
            boolean isCartEmpty = cart.getItems() == null || cart.getItems().isEmpty();

            if (!isCartEmpty && !cart.getRestaurant().getId().equals(restaurant.getId())) {
                throw new ValidationException(
                        "Your cart has items from " + cart.getRestaurant().getName() +
                                ". Clear your cart first to order from " + restaurant.getName()
                );
            }

          // If cart is empty → update restaurant
            if (isCartEmpty) {
                cart.setRestaurant(restaurant);
            }
        }

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndMenuItemId(cart.getId(), menuItem.getId());

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setMenuItem(menuItem);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setPrice(menuItem.getPrice());
            cartItemRepository.save(cartItem);
        }

        Cart freshCart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return buildCartResponse(freshCart);
    }

    // =====================================================
    // GET CART
    // =====================================================
    @Override
    public CartResponseDto getCart(String token) {

        UserEntity customer = getUserFromToken(token);

        Cart cart = cartRepository.findByUserId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart is empty"));

        return buildCartResponse(cart);
    }

    // =====================================================
    // UPDATE ITEM
    // =====================================================
    @Override
    public CartResponseDto updateItem(Long cartItemId, Integer quantity, String token) {

        UserEntity customer = getUserFromToken(token);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));


        if (!cartItem.getCart().getUser().getId().equals(customer.getId())) {
            throw new UnauthorizedException("You are not authorized to update this cart item");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return buildCartResponse(cartItem.getCart());
    }

    // =====================================================
    // REMOVE ITEM
    // =====================================================
    @Override
    public CartResponseDto removeItem(Long cartItemId, String token) {

        UserEntity customer = getUserFromToken(token);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));


        if (!cartItem.getCart().getUser().getId().equals(customer.getId())) {
            throw new UnauthorizedException("You are not authorized to remove this cart item");
        }

        Cart cart = cartItem.getCart();
        cartItemRepository.deleteById(cartItemId);

        cart = cartRepository.findById(cart.getId()).get();
        return buildCartResponse(cart);
    }

    // =====================================================
    // CLEAR CART
    // =====================================================
    @Override
    @Transactional
    public void clearCart(String token) {

        UserEntity customer = getUserFromToken(token);


        Cart cart = cartRepository.findByUserId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart is empty"));

        cartRepository.deleteById(cart.getId());
    }

    // =====================================================
    // HELPER: GET USER
    // =====================================================
    private UserEntity getUserFromToken(String token) {
        String email = jwtService.extractEmail(token.substring(7));
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // =====================================================
    // HELPER: BUILD RESPONSE
    // =====================================================
    private CartResponseDto buildCartResponse(Cart cart) {

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