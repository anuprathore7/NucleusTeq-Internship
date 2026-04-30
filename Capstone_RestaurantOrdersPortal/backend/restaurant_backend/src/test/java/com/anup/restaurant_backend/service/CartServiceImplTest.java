package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.CartItemRequestDto;
import com.anup.restaurant_backend.dto.CartResponseDto;
import com.anup.restaurant_backend.entity.*;
import com.anup.restaurant_backend.enums.UserRole;
import com.anup.restaurant_backend.exception.ResourceNotFoundException;
import com.anup.restaurant_backend.exception.UnauthorizedException;
import com.anup.restaurant_backend.exception.ValidationException;
import com.anup.restaurant_backend.repository.*;
import com.anup.restaurant_backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ============================================================
 *  CartServiceImplTest — Unit Tests for Business Logic
 * ============================================================
 *
 *  WHAT WE TEST HERE:
 *  All business rules in CartServiceImpl:
 *  1. Only one restaurant at a time in cart
 *  2. Duplicate item → quantity increases, no new row
 *  3. Item must be available
 *  4. Security: only cart owner can update/remove
 *  5. Empty cart returns 404
 *  6. Wallet deduction happens (tested in OrderService)
 *
 *  HOW MOCKING WORKS:
 *  @Mock creates fake versions of repositories + JwtService
 *  We control what they return using when(...).thenReturn(...)
 *  This way we test ONLY the service logic, not DB
 * ============================================================
 */
class CartServiceImplTest {

    // ── Mocked dependencies ──────────────────────────────────
    @Mock private CartRepository       cartRepository;
    @Mock private CartItemRepository   cartItemRepository;
    @Mock private MenuItemRepository   menuItemRepository;
    @Mock private UserRepository       userRepository;
    @Mock private JwtService           jwtService;

    // ── Class under test ──────────────────────────────────────
    @InjectMocks
    private CartServiceImpl cartService;

    // ── Test constants ────────────────────────────────────────
    private static final String TOKEN       = "Bearer test.jwt.token";
    private static final String EMAIL       = "rahul@gmail.com";
    private static final Long   USER_ID     = 7L;
    private static final Long   REST_ID_1   = 5L;  // Pizza Hub
    private static final Long   REST_ID_2   = 6L;  // Burger King
    private static final Long   ITEM_ID     = 3L;

    // ── Test entities ─────────────────────────────────────────
    private UserEntity   mockUser;
    private Restaurant   mockRestaurant1;
    private Restaurant   mockRestaurant2;
    private MenuItem     mockMenuItem;
    private Cart         mockCart;

    // ─────────────────────────────────────────────────────────
    //  SETUP — runs before every @Test
    // ─────────────────────────────────────────────────────────
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Build mock user
        mockUser = new UserEntity();
        mockUser.setId(USER_ID);
        mockUser.setEmail(EMAIL);
        mockUser.setRole(UserRole.USER);
        mockUser.setWalletBalance(1000.0);

        // Build mock restaurants
        mockRestaurant1 = buildRestaurant(REST_ID_1, "Pizza Hub");
        mockRestaurant2 = buildRestaurant(REST_ID_2, "Burger King");

        // Build mock menu item (belongs to Pizza Hub)
        mockMenuItem = new MenuItem();
        mockMenuItem.setId(ITEM_ID);
        mockMenuItem.setName("Paneer Pizza");
        mockMenuItem.setPrice(299.0);
        mockMenuItem.setAvailable(true);
        mockMenuItem.setRestaurant(mockRestaurant1);

        // Build mock cart (linked to Pizza Hub)
        mockCart = new Cart();
        mockCart.setId(1L);
        mockCart.setUser(mockUser);
        mockCart.setRestaurant(mockRestaurant1);
        mockCart.setItems(new ArrayList<>());

        // Default JWT mock — always returns our test email
        when(jwtService.extractEmail("test.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
    }

    // =========================================================
    //  ADD ITEM TESTS
    // =========================================================

    @Test
    @DisplayName("addItem — new cart created when user has no cart")
    void addItem_noExistingCart_createsNewCart() {
        // ── ARRANGE ───────────────────────────────────────────
        CartItemRequestDto request = buildRequest(ITEM_ID, 1);

        when(menuItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(mockMenuItem));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty()); // no cart yet
        when(cartRepository.save(any(Cart.class))).thenReturn(mockCart);
        when(cartItemRepository.findByCartIdAndMenuItemId(any(), eq(ITEM_ID)))
                .thenReturn(Optional.empty()); // item not in cart yet
        when(cartRepository.findById(mockCart.getId())).thenReturn(Optional.of(mockCart));

        // ── ACT ───────────────────────────────────────────────
        CartResponseDto result = cartService.addItem(request, TOKEN);

        // ── ASSERT ────────────────────────────────────────────
        assertNotNull(result, "Result should not be null");

        // A new Cart must be saved
        verify(cartRepository, times(1)).save(any(Cart.class));

        // CartItem must be saved for the new item
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("addItem — same item added twice: quantity increases, no duplicate row")
    void addItem_duplicateItem_quantityIncreased() {
        // ── ARRANGE ───────────────────────────────────────────
        CartItemRequestDto request = buildRequest(ITEM_ID, 1);

        // Existing CartItem with quantity 1
        CartItem existingItem = buildCartItem(1L, mockCart, mockMenuItem, 1, 299.0);

        when(menuItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(mockMenuItem));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockCart));
        when(cartItemRepository.findByCartIdAndMenuItemId(mockCart.getId(), ITEM_ID))
                .thenReturn(Optional.of(existingItem)); // item already exists!
        when(cartRepository.findById(mockCart.getId())).thenReturn(Optional.of(mockCart));

        // ── ACT ───────────────────────────────────────────────
        cartService.addItem(request, TOKEN);

        // ── ASSERT ────────────────────────────────────────────
        // Quantity should be updated on existing item (2 = 1 + 1)
        assertEquals(2, existingItem.getQuantity(),
                "Quantity should increase from 1 to 2, not create new row");

        // cartItem.save called to update (not create new)
        verify(cartItemRepository, times(1)).save(existingItem);

        // cartRepository.save NOT called again (no new cart)
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("addItem — different restaurant: throws ValidationException")
    void addItem_differentRestaurant_throwsValidationException() {
        // ── ARRANGE ───────────────────────────────────────────
        // Cart has Pizza Hub items
        // New item is from Burger King
        MenuItem burgerKingItem = new MenuItem();
        burgerKingItem.setId(50L);
        burgerKingItem.setName("Zinger Burger");
        burgerKingItem.setPrice(199.0);
        burgerKingItem.setAvailable(true);
        burgerKingItem.setRestaurant(mockRestaurant2); // different restaurant!

        CartItemRequestDto request = buildRequest(50L, 1);

        when(menuItemRepository.findById(50L)).thenReturn(Optional.of(burgerKingItem));
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockCart)); // cart has Pizza Hub

        // ── ACT + ASSERT ──────────────────────────────────────
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> cartService.addItem(request, TOKEN),
                "Should throw when adding item from different restaurant"
        );

        assertTrue(ex.getMessage().contains("Pizza Hub"),
                "Error message should mention existing restaurant");
        assertTrue(ex.getMessage().contains("Burger King"),
                "Error message should mention new restaurant");

        // No cart item should be savedx`
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("addItem — item not found: throws ResourceNotFoundException")
    void addItem_menuItemNotFound_throwsResourceNotFoundException() {
        // ── ARRANGE ───────────────────────────────────────────
        CartItemRequestDto request = buildRequest(999L, 1);

        when(menuItemRepository.findById(999L)).thenReturn(Optional.empty());

        // ── ACT + ASSERT ──────────────────────────────────────
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addItem(request, TOKEN)
        );

        assertTrue(ex.getMessage().contains("Menu item not found"));

        // Nothing should be saved
        verify(cartItemRepository, never()).save(any());
        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("addItem — unavailable item: throws ValidationException")
    void addItem_unavailableItem_throwsValidationException() {
        // ── ARRANGE ───────────────────────────────────────────
        mockMenuItem.setAvailable(false); // mark as unavailable
        CartItemRequestDto request = buildRequest(ITEM_ID, 1);

        when(menuItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(mockMenuItem));

        // ── ACT + ASSERT ──────────────────────────────────────
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> cartService.addItem(request, TOKEN)
        );

        assertEquals("Sorry, this item is currently unavailable.", ex.getMessage());
        verify(cartItemRepository, never()).save(any());
    }

    // =========================================================
    //  GET CART TESTS
    // =========================================================

    @Test
    @DisplayName("getCart — success: returns cart with items and total")
    void getCart_cartExists_returnsCartResponse() {
        // ── ARRANGE ───────────────────────────────────────────
        CartItem cartItem = buildCartItem(1L, mockCart, mockMenuItem, 2, 299.0);
        mockCart.setItems(List.of(cartItem));

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockCart));

        // ── ACT ───────────────────────────────────────────────
        CartResponseDto result = cartService.getCart(TOKEN);

        // ── ASSERT ────────────────────────────────────────────
        assertNotNull(result);
        assertEquals(1L, result.getCartId());
        assertEquals("Pizza Hub", result.getRestaurantName());
        assertEquals(1, result.getItems().size(), "Should have 1 item");
        assertEquals(598.0, result.getTotalAmount(), 0.01, "Total = 299 x 2 = 598");

        verify(cartRepository, times(1)).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("getCart — no cart: throws ResourceNotFoundException")
    void getCart_noCart_throwsResourceNotFoundException() {
        // ── ARRANGE ───────────────────────────────────────────
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        // ── ACT + ASSERT ──────────────────────────────────────
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.getCart(TOKEN)
        );

        assertEquals("Cart is empty", ex.getMessage());
    }

    // =========================================================
    //  UPDATE ITEM TESTS
    // =========================================================

    @Test
    @DisplayName("updateItem — success: quantity updated correctly")
    void updateItem_validRequest_quantityUpdated() {
        // ── ARRANGE ───────────────────────────────────────────
        Long cartItemId = 1L;
        CartItem cartItem = buildCartItem(cartItemId, mockCart, mockMenuItem, 1, 299.0);
        mockCart.setItems(List.of(cartItem));

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));

        // ── ACT ───────────────────────────────────────────────
        cartService.updateItem(cartItemId, 3, TOKEN);

        // ── ASSERT ────────────────────────────────────────────
        assertEquals(3, cartItem.getQuantity(), "Quantity should be updated to 3");
        verify(cartItemRepository, times(1)).save(cartItem);
    }

    @Test
    @DisplayName("updateItem — wrong user: throws UnauthorizedException")
    void updateItem_wrongUser_throwsUnauthorizedException() {
        // ── ARRANGE ───────────────────────────────────────────
        // Create a different user who owns the cart
        UserEntity otherUser = new UserEntity();
        otherUser.setId(99L); // different ID from mockUser (ID=7)

        Cart otherUserCart = new Cart();
        otherUserCart.setId(10L);
        otherUserCart.setUser(otherUser); // different user!

        CartItem cartItem = buildCartItem(1L, otherUserCart, mockMenuItem, 1, 299.0);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        // ── ACT + ASSERT ──────────────────────────────────────
        assertThrows(
                UnauthorizedException.class,
                () -> cartService.updateItem(1L, 2, TOKEN),
                "Should throw when user tries to update another user's cart item"
        );

        // CartItem should NOT be saved
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateItem — cart item not found: throws ResourceNotFoundException")
    void updateItem_cartItemNotFound_throwsResourceNotFoundException() {
        // ── ARRANGE ───────────────────────────────────────────
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        // ── ACT + ASSERT ──────────────────────────────────────
        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.updateItem(999L, 2, TOKEN)
        );
    }

    // =========================================================
    //  REMOVE ITEM TESTS
    // =========================================================

    @Test
    @DisplayName("removeItem — success: item deleted from DB")
    void removeItem_validItem_itemDeleted() {
        // ── ARRANGE ───────────────────────────────────────────
        Long cartItemId = 1L;
        CartItem cartItem = buildCartItem(cartItemId, mockCart, mockMenuItem, 1, 299.0);
        mockCart.setItems(new ArrayList<>(List.of(cartItem)));

        when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(cartItem));
        when(cartRepository.findById(mockCart.getId())).thenReturn(Optional.of(mockCart));

        // ── ACT ───────────────────────────────────────────────
        cartService.removeItem(cartItemId, TOKEN);

        // ── ASSERT ────────────────────────────────────────────
        // deleteById must be called on the cartItem
        verify(cartItemRepository, times(1)).deleteById(cartItemId);
    }

    @Test
    @DisplayName("removeItem — wrong user: throws UnauthorizedException")
    void removeItem_wrongUser_throwsUnauthorizedException() {
        // ── ARRANGE ───────────────────────────────────────────
        UserEntity otherUser = new UserEntity();
        otherUser.setId(99L);

        Cart otherCart = new Cart();
        otherCart.setId(10L);
        otherCart.setUser(otherUser);

        CartItem cartItem = buildCartItem(1L, otherCart, mockMenuItem, 1, 299.0);

        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        // ── ACT + ASSERT ──────────────────────────────────────
        assertThrows(
                UnauthorizedException.class,
                () -> cartService.removeItem(1L, TOKEN)
        );

        verify(cartItemRepository, never()).deleteById(any());
    }

    // =========================================================
    //  CLEAR CART TESTS
    // =========================================================

    @Test
    @DisplayName("clearCart — success: cart deleted from DB")
    void clearCart_cartExists_cartDeleted() {
        // ── ARRANGE ───────────────────────────────────────────
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(mockCart));
        doNothing().when(cartRepository).deleteById(mockCart.getId());

        // ── ACT ───────────────────────────────────────────────
        cartService.clearCart(TOKEN);

        // ── ASSERT ────────────────────────────────────────────
        verify(cartRepository, times(1)).deleteById(mockCart.getId());
    }

    @Test
    @DisplayName("clearCart — no cart: throws ResourceNotFoundException")
    void clearCart_noCart_throwsResourceNotFoundException() {
        // ── ARRANGE ───────────────────────────────────────────
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        // ── ACT + ASSERT ──────────────────────────────────────
        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.clearCart(TOKEN)
        );

        // deleteById must NOT be called
        verify(cartRepository, never()).deleteById(any());
    }

    // =========================================================
    //  HELPER METHODS
    // =========================================================

    private CartItemRequestDto buildRequest(Long menuItemId, Integer quantity) {
        CartItemRequestDto r = new CartItemRequestDto();
        r.setMenuItemId(menuItemId);
        r.setQuantity(quantity);
        return r;
    }

    private CartItem buildCartItem(Long id, Cart cart, MenuItem menuItem,
                                   Integer quantity, Double price) {
        CartItem item = new CartItem();
        item.setId(id);
        item.setCart(cart);
        item.setMenuItem(menuItem);
        item.setQuantity(quantity);
        item.setPrice(price);
        return item;
    }

    private Restaurant buildRestaurant(Long id, String name) {
        Restaurant r = new Restaurant();
        r.setId(id);
        r.setName(name);
        return r;
    }
}