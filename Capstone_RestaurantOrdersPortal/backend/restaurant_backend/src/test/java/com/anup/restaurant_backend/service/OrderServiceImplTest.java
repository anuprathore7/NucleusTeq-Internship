package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.entity.*;
import com.anup.restaurant_backend.enums.OrderStatus;
import com.anup.restaurant_backend.repository.*;
import com.anup.restaurant_backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for OrderServiceImpl.
 *
 * This class verifies complete order lifecycle:
 * - Placing orders
 * - Fetching orders
 * - Cancelling orders
 * - Updating order status
 *
 * It also validates edge cases like:
 * - Unauthorized access
 * - Empty cart
 * - Insufficient wallet balance
 * - Invalid status transitions
 */
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private UserRepository userRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private JwtService jwtService;

    @InjectMocks private OrderServiceImpl orderService;

    private UserEntity user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = new UserEntity();
        user.setId(1L);
        user.setEmail("test@mail.com");
        user.setWalletBalance(1000.0);

        when(jwtService.extractEmail(anyString())).thenReturn(user.getEmail());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    }

    /**
     * Tests successful order placement.
     *
     * Scenario:
     * - Valid cart
     * - Valid address
     * - Sufficient wallet balance
     *
     * Expected:
     * - Order is created
     * - Wallet is deducted
     * - Cart is cleared
     */
    @Test
    void placeOrder_success() {

        Address address = new Address();
        address.setId(1L);
        address.setUser(user);

        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Test Rest");

        MenuItem menuItem = new MenuItem();
        menuItem.setName("Pizza");

        CartItem cartItem = new CartItem();
        cartItem.setMenuItem(menuItem);
        cartItem.setPrice(100.0);
        cartItem.setQuantity(2);

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setRestaurant(restaurant);
        cart.setItems(List.of(cartItem));

        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        var result = orderService.placeOrder("Bearer token", 1L);

        assertEquals(200.0, result.getTotalAmount());
        verify(cartRepository).deleteById(1L);
    }

    /**
     * Tests failure when cart is empty.
     */
    @Test
    void placeOrder_cartEmpty() {

        Address address = new Address();
        address.setUser(user);

        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> orderService.placeOrder("Bearer token", 1L));
    }

    /**
     * Tests failure when wallet balance is insufficient.
     */
    @Test
    void placeOrder_insufficientBalance() {

        user.setWalletBalance(50.0);

        Address address = new Address();
        address.setUser(user);

        MenuItem menuItem = new MenuItem();
        menuItem.setName("Burger");

        CartItem item = new CartItem();
        item.setMenuItem(menuItem);
        item.setPrice(100.0);
        item.setQuantity(1);

        Cart cart = new Cart();
        cart.setItems(List.of(item));

        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThrows(RuntimeException.class,
                () -> orderService.placeOrder("Bearer token", 1L));
    }

    /**
     * Tests fetching user orders.
     */
    @Test
    void getMyOrders_success() {

        Order order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());

        Restaurant r = new Restaurant();
        r.setId(1L);
        r.setName("Test");
        order.setRestaurant(r);

        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(order));

        var result = orderService.getMyOrders("Bearer token");

        assertEquals(1, result.size());
    }

    /**
     * Tests successful cancellation within allowed time.
     */
    @Test
    void cancelOrder_success() {

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalAmount(100.0);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        var result = orderService.cancelOrder(1L, "Bearer token");

        assertEquals("CANCELLED", result.getStatus());
    }

    /**
     * Tests cancellation failure after time window expires.
     */
    @Test
    void cancelOrder_timeExpired() {

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now().minusMinutes(1));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class,
                () -> orderService.cancelOrder(1L, "Bearer token"));
    }

    /**
     * Tests fetching restaurant orders by owner.
     */
    @Test
    void getRestaurantOrders_success() {

        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setOwner(user);

        when(restaurantRepository.findById(1L))
                .thenReturn(Optional.of(restaurant));
        when(orderRepository.findByRestaurantIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        var result = orderService.getRestaurantOrders(1L, "Bearer token");

        assertNotNull(result);
    }

    /**
     * Tests updating order status successfully.
     */
    @Test
    void updateOrderStatus_success() {

        Restaurant restaurant = new Restaurant();
        restaurant.setOwner(user);

        Order order = new Order();
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.PLACED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        var result = orderService.updateOrderStatus(1L, "PENDING", "Bearer token");

        assertEquals("PENDING", result.getStatus());
    }

    /**
     * Tests invalid status transition.
     */
    @Test
    void updateOrderStatus_invalidTransition() {

        Restaurant restaurant = new Restaurant();
        restaurant.setOwner(user);

        Order order = new Order();
        order.setRestaurant(restaurant);
        order.setStatus(OrderStatus.PLACED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class,
                () -> orderService.updateOrderStatus(1L, "COMPLETED", "Bearer token"));
    }
}