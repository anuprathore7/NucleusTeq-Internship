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
 * Service implementation for handling order operations such as placing,
 * fetching, cancelling, and updating order status.
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository      orderRepository;
    private final CartRepository       cartRepository;
    private final UserRepository       userRepository;
    private final RestaurantRepository restaurantRepository;
    private final AddressRepository    addressRepository;
    private final JwtService           jwtService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CartRepository cartRepository,
                            UserRepository userRepository,
                            RestaurantRepository restaurantRepository,
                            AddressRepository addressRepository,
                            JwtService jwtService) {
        this.orderRepository      = orderRepository;
        this.cartRepository       = cartRepository;
        this.userRepository       = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.addressRepository    = addressRepository;
        this.jwtService           = jwtService;
    }

    /**
     * Places an order for the authenticated user using items from their cart.
     * Validates the delivery address, deducts wallet balance, and clears the cart.
     */
    @Override
    @Transactional
    public OrderResponseDto placeOrder(String token, Long deliveryAddressId) {

        UserEntity customer = getUserFromToken(token);

        // Validate delivery address belongs to this user
        Address deliveryAddress = addressRepository.findById(deliveryAddressId)
                .orElseThrow(() -> new RuntimeException("Delivery address not found."));

        if (!deliveryAddress.getUser().getId().equals(customer.getId())) {
            throw new RuntimeException("You are not authorized to use this address.");
        }

        Cart cart = cartRepository.findByUserId(customer.getId())
                .orElseThrow(() -> new RuntimeException("Your cart is empty. Add items before placing order."));

        List<CartItem> cartItems = cart.getItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Your cart is empty. Add items before placing order.");
        }

        Double total = cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        if (customer.getWalletBalance() < total) {
            throw new RuntimeException(
                    "Insufficient wallet balance. Required: ₹" + total +
                            ", Available: ₹" + customer.getWalletBalance()
            );
        }

        Order order = new Order();
        order.setUser(customer);
        order.setRestaurant(cart.getRestaurant());
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());
        order.setDeliveryAddress(deliveryAddress);

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            OrderItem oi = new OrderItem();
            oi.setOrder(savedOrder);
            oi.setMenuItem(cartItem.getMenuItem());
            oi.setItemName(cartItem.getMenuItem().getName());
            oi.setPrice(cartItem.getPrice());
            oi.setQuantity(cartItem.getQuantity());
            return oi;
        }).collect(Collectors.toList());

        savedOrder.setOrderItems(orderItems);
        orderRepository.save(savedOrder);

        customer.setWalletBalance(customer.getWalletBalance() - total);
        userRepository.save(customer);

        cartRepository.deleteById(cart.getId());

        log.info("Order placed. OrderId={}, UserId={}, Total=₹{}, Address={}",
                savedOrder.getId(), customer.getId(), total, deliveryAddress.getId());

        return mapToResponse(savedOrder);
    }

    /**
     * Retrieves all orders of the authenticated user sorted by latest first.
     */
    @Override
    public List<OrderResponseDto> getMyOrders(String token) {
        UserEntity customer = getUserFromToken(token);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(customer.getId());
        return orders.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Cancels an order within 30 seconds if it is still in PLACED status.
     * Refunds the total amount to the user's wallet.
     */
    @Override
    @Transactional
    public OrderResponseDto cancelOrder(Long orderId, String token) {

        UserEntity customer = getUserFromToken(token);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getUser().getId().equals(customer.getId())) {
            throw new RuntimeException("You are not authorized to cancel this order.");
        }

        if (order.getStatus() != OrderStatus.PLACED) {
            throw new RuntimeException(
                    "Cannot cancel order. Current status: " + order.getStatus() +
                            ". Only PLACED orders can be cancelled."
            );
        }

        long secondsElapsed = ChronoUnit.SECONDS.between(order.getCreatedAt(), LocalDateTime.now());
        if (secondsElapsed > 30) {
            throw new RuntimeException(
                    "Cancellation window has expired. Orders can only be cancelled within 30 seconds of placing."
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        customer.setWalletBalance(customer.getWalletBalance() + order.getTotalAmount());
        userRepository.save(customer);

        log.info("Order {} cancelled. ₹{} refunded to userId={}", orderId, order.getTotalAmount(), customer.getId());

        return mapToResponse(order);
    }

    /**
     * Retrieves all orders for a restaurant owned by the authenticated user.
     */
    @Override
    public List<OrderResponseDto> getRestaurantOrders(Long restaurantId, String token) {

        UserEntity owner = getUserFromToken(token);

        restaurantRepository.findById(restaurantId)
                .filter(r -> r.getOwner().getId().equals(owner.getId()))
                .orElseThrow(() -> new RuntimeException("Restaurant not found or you don't own it."));

        List<Order> orders = orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
        return orders.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Updates the status of an order based on valid lifecycle transitions.
     * Flow: PLACED -> PENDING -> ACCEPTED -> OUT_FOR_DELIVERY -> COMPLETED.
     */
    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, String status, String token) {

        UserEntity owner = getUserFromToken(token);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!order.getRestaurant().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You are not authorized to update this order.");
        }

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status +
                    ". Valid values: PLACED, PENDING, ACCEPTED, OUT_FOR_DELIVERY, COMPLETED, CANCELLED");
        }

        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        orderRepository.save(order);

        log.info("Order {} status updated to {} by owner={}", orderId, newStatus, owner.getEmail());

        return mapToResponse(order);
    }

    /**
     * Validates that the requested status transition follows the allowed order lifecycle.
     */
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (next == OrderStatus.CANCELLED) return;

        boolean valid = switch (current) {
            case PLACED           -> next == OrderStatus.PENDING;
            case PENDING          -> next == OrderStatus.ACCEPTED;
            case ACCEPTED         -> next == OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> next == OrderStatus.COMPLETED;
            default               -> false;
        };

        if (!valid) {
            throw new RuntimeException(
                    "Invalid status transition: " + current + " to " + next +
                            ". Expected flow: PLACED -> PENDING -> ACCEPTED -> OUT_FOR_DELIVERY -> COMPLETED"
            );
        }
    }

    /**
     * Extracts the authenticated user from the Bearer token.
     */
    private UserEntity getUserFromToken(String token) {
        String email = jwtService.extractEmail(token.substring(7));
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Converts an Order entity into an OrderResponseDto.
     */
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

        // Build delivery address string if present
        String addressStr = null;
        if (order.getDeliveryAddress() != null) {
            Address a = order.getDeliveryAddress();
            addressStr = a.getStreet() + ", " + a.getCity() + ", " + a.getState() + " - " + a.getPincode();
        }

        return new OrderResponseDto(
                order.getId(),
                order.getRestaurant().getId(),
                order.getRestaurant().getName(),
                itemDtos,
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt(),
                addressStr
        );
    }
}