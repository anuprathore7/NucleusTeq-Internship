package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.CartItemRequestDto;
import com.anup.restaurant_backend.dto.CartResponseDto;
import com.anup.restaurant_backend.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CartController}.
 * Verifies that each endpoint correctly delegates to {@link CartService}
 * and returns the expected response without starting a server.
 */
@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private CartItemRequestDto requestDto;
    private CartResponseDto cartResponse;

    private static final String TOKEN = "Bearer mocked.jwt.token";
    private static final Long CART_ITEM_ID = 1L;

    /**
     * Builds shared request and response objects before each test
     * so individual tests stay clean and focused.
     */
    @BeforeEach
    void setUp() {
        requestDto = new CartItemRequestDto();
        requestDto.setMenuItemId(50L);
        requestDto.setQuantity(2);

        cartResponse = new CartResponseDto(
                5L, 10L, "Spice Garden",
                List.of(), 500.0
        );
    }

    /**
     * Verifies that adding an item delegates to the service with the correct
     * request and token, then returns the updated cart.
     */
    @Test
    void addItem_shouldReturnUpdatedCart_whenRequestIsValid() {
        when(cartService.addItem(requestDto, TOKEN)).thenReturn(cartResponse);

        CartResponseDto result = cartController.addItem(requestDto, TOKEN);

        assertNotNull(result);
        assertEquals("Spice Garden", result.getRestaurantName());
        assertEquals(500.0, result.getTotalAmount());
        verify(cartService, times(1)).addItem(requestDto, TOKEN);
    }

    /**
     * Verifies that fetching the cart returns the cart wrapped in a 200 response
     * when the cart exists for the user.
     */
    @Test
    void getCart_shouldReturn200_whenCartExists() {
        when(cartService.getCart(TOKEN)).thenReturn(cartResponse);

        ResponseEntity<?> response = cartController.getCart(TOKEN);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(cartService, times(1)).getCart(TOKEN);
    }

    /**
     * Verifies that fetching the cart returns an empty cart with zero total
     * when the service throws an exception because no cart exists.
     */
    @Test
    void getCart_shouldReturnEmptyCart_whenCartDoesNotExist() {
        when(cartService.getCart(TOKEN)).thenThrow(new RuntimeException("Cart not found"));

        ResponseEntity<?> response = cartController.getCart(TOKEN);

        assertEquals(200, response.getStatusCode().value());
        CartResponseDto body = (CartResponseDto) response.getBody();
        assertNotNull(body);
        assertEquals(0.0, body.getTotalAmount());
        assertTrue(body.getItems().isEmpty());
    }

    /**
     * Verifies that updating an item quantity delegates to the service
     * with the correct cart item ID, quantity, and token.
     */
    @Test
    void updateItem_shouldReturnUpdatedCart_whenRequestIsValid() {
        when(cartService.updateItem(CART_ITEM_ID, 3, TOKEN)).thenReturn(cartResponse);

        CartResponseDto result = cartController.updateItem(CART_ITEM_ID, 3, TOKEN);

        assertNotNull(result);
        assertEquals("Spice Garden", result.getRestaurantName());
        verify(cartService, times(1)).updateItem(CART_ITEM_ID, 3, TOKEN);
    }

    /**
     * Verifies that removing an item delegates to the service with the correct
     * cart item ID and token, then returns the updated cart.
     */
    @Test
    void removeItem_shouldReturnUpdatedCart_whenItemExists() {
        when(cartService.removeItem(CART_ITEM_ID, TOKEN)).thenReturn(cartResponse);

        CartResponseDto result = cartController.removeItem(CART_ITEM_ID, TOKEN);

        assertNotNull(result);
        assertEquals("Spice Garden", result.getRestaurantName());
        verify(cartService, times(1)).removeItem(CART_ITEM_ID, TOKEN);
    }

    /**
     * Verifies that clearing the cart calls the service once and returns
     * the expected success message string.
     */
    @Test
    void clearCart_shouldReturnSuccessMessage_whenCartIsCleared() {
        doNothing().when(cartService).clearCart(TOKEN);

        String result = cartController.clearCart(TOKEN);

        assertEquals("Cart cleared successfully", result);
        verify(cartService, times(1)).clearCart(TOKEN);
    }

    /**
     * Verifies that the controller never handles cart logic itself
     * and always passes the token through to the service unchanged.
     */
    @Test
    void addItem_shouldPassTokenToService_unchanged() {
        when(cartService.addItem(any(CartItemRequestDto.class), eq(TOKEN)))
                .thenReturn(cartResponse);

        cartController.addItem(requestDto, TOKEN);

        verify(cartService).addItem(any(CartItemRequestDto.class), eq(TOKEN));
    }
}