package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.OrderItemResponseDto;
import com.anup.restaurant_backend.dto.OrderResponseDto;
import com.anup.restaurant_backend.entity.*;
import com.anup.restaurant_backend.enums.OrderStatus;
import com.anup.restaurant_backend.exception.ResourceNotFoundException;
import com.anup.restaurant_backend.repository.*;
import com.anup.restaurant_backend.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ============================================
 *   OrderServiceImpl
 * ============================================
 * All steps are @Transactional — if anything fails,
 * everything rolls back. No partial state!
 *
 *  30 SECOND CANCELLATION RULE (SRS FR-12):
 * createdAt is stored when order is placed.
 * On cancel → check: now - createdAt <= 30 seconds
 * If more than 30 seconds → reject cancellation.
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final JwtService jwtService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartRepository cartRepository,
                            UserRepository userRepository,
                            RestaurantRepository restaurantRepository,
                            JwtService jwtService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.jwtService = jwtService;
    }

    // =====================================================
    //  PLACE ORDER
    // =====================================================

    @Override
    @Transactional
    public OrderResponseDto placeOrder(String token) {

        // Step 1: Get customer from JWT
        UserEntity customer = getUserFromToken(token);

        // Step 2: Get their cart
        Cart cart = cartRepository.findByUserId(customer.getId())
                .orElseThrow(() -> new RuntimeException("Your cart is empty. Add items before placing order."));

        // Step 3: Check cart has items
        List<CartItem> cartItems = cart.getItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Your cart is empty. Add items before placing order.");
        }

        // Step 4: Calculate total
        Double total = cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        // Step 5: Check wallet balance
        if (customer.getWalletBalance() < total) {
            throw new RuntimeException(
                    "Insufficient wallet balance. Required: ₹" + total +
                            ", Available: ₹" + customer.getWalletBalance()
            );
        }

        // Step 6: Create order
        Order order = new Order();
        order.setUser(customer);
        order.setRestaurant(cart.getRestaurant());
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Step 7: Copy cart items → OrderItems (SNAPSHOT)
        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            OrderItem oi = new OrderItem();
            oi.setOrder(savedOrder);
            oi.setMenuItem(cartItem.getMenuItem());
            oi.setItemName(cartItem.getMenuItem().getName()); // snapshot name
            oi.setPrice(cartItem.getPrice());                  // snapshot price
            oi.setQuantity(cartItem.getQuantity());
            return oi;
        }).collect(Collectors.toList());

        savedOrder.setOrderItems(orderItems);
        orderRepository.save(savedOrder);

        // Step 8: Deduct wallet balance
        customer.setWalletBalance(customer.getWalletBalance() - total);
        userRepository.save(customer);

        // Step 9: Clear the cart (delete it — cascade removes cart items)
        cartRepository.deleteById(cart.getId());

        log.info("Order placed successfully. OrderId: {}, UserId: {}, Total: ₹{}",
                savedOrder.getId(), customer.getId(), total);

        // Step 10: Return response
        return mapToResponse(savedOrder);
    }

    // =====================================================
    //  GET MY ORDERS (Customer order history)
    // =====================================================

    @Override
    public List<OrderResponseDto> getMyOrders(String token) {
        UserEntity customer = getUserFromToken(token);

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(customer.getId());

        log.info("Fetched {} orders for userId: {}", orders.size(), customer.getId());

        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    //  CANCEL ORDER (30 second rule)
    // =====================================================

    @Override
    @Transactional
    public OrderResponseDto cancelOrder(Long orderId, String token) {

        UserEntity customer = getUserFromToken(token);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Check this order belongs to this customer
        if (!order.getUser().getId().equals(customer.getId())) {
            throw new RuntimeException("You are not authorized to cancel this order.");
        }

        // Check order is in PLACED status (can only cancel PLACED orders)
        if (order.getStatus() != OrderStatus.PLACED) {
            throw new RuntimeException(
                    "Cannot cancel order. Current status: " + order.getStatus() +
                            ". Only PLACED orders can be cancelled."
            );
        }

        // ── 30 SECOND RULE (SRS FR-12) ──
        long secondsElapsed = ChronoUnit.SECONDS.between(order.getCreatedAt(), LocalDateTime.now());

        if (secondsElapsed > 30) {
            throw new RuntimeException(
                    "Cancellation window has expired. Orders can only be cancelled within 30 seconds of placing."
            );
        }

        // Cancel the order
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Refund wallet balance
        customer.setWalletBalance(customer.getWalletBalance() + order.getTotalAmount());
        userRepository.save(customer);

        log.info("Order {} cancelled by userId: {}. ₹{} refunded to wallet.",
                orderId, customer.getId(), order.getTotalAmount());

        return mapToResponse(order);
    }

    // =====================================================
    //  GET RESTAURANT ORDERS (Owner view)
    // =====================================================

    @Override
    public List<OrderResponseDto> getRestaurantOrders(Long restaurantId, String token) {

        UserEntity owner = getUserFromToken(token);

        // Verify owner owns this restaurant
        restaurantRepository.findById(restaurantId)
                .filter(r -> r.getOwner().getId().equals(owner.getId()))
                .orElseThrow(() -> new RuntimeException("Restaurant not found or you don't own it."));

        List<Order> orders = orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);

        log.info("Fetched {} orders for restaurantId: {}", orders.size(), restaurantId);

        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // =====================================================
    //  UPDATE ORDER STATUS (Owner)
    // =====================================================

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, String status, String token) {

        UserEntity owner = getUserFromToken(token);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Verify owner owns the restaurant for this order
        if (!order.getRestaurant().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You are not authorized to update this order.");
        }

        // Parse and set new status
        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status +
                    ". Valid values: PENDING, DELIVERED, COMPLETED, CANCELLED");
        }

        orderRepository.save(order);

        log.info("Order {} status updated to {} by owner: {}", orderId, status, owner.getEmail());

        return mapToResponse(order);
    }

    // =====================================================
    //  PRIVATE HELPERS
    // =====================================================

    private UserEntity getUserFromToken(String token) {
        String email = jwtService.extractEmail(token.substring(7));
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private OrderResponseDto mapToResponse(Order order) {
        List<OrderItemResponseDto> itemDtos = order.getOrderItems() == null
                ? List.of()
                : order.getOrderItems().stream()
                  .map(oi -> new OrderItemResponseDto(
                          oi.getId(),
                          oi.getItemName(),
                          oi.getPrice(),
                          oi.getQuantity(),
                          oi.getPrice() * oi.getQuantity()
                  ))
                  .collect(Collectors.toList());

        return new OrderResponseDto(
                order.getId(),
                order.getRestaurant().getId(),
                order.getRestaurant().getName(),
                itemDtos,
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt()
        );
    }
}