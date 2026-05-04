package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.OrderResponseDto;
import com.anup.restaurant_backend.security.CustomUserDetailsService;
import com.anup.restaurant_backend.service.OrderService;
import com.anup.restaurant_backend.security.JwtService; // ✅ ADDED
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller layer tests for {@link OrderController}.
 * Covers placing orders, fetching order history, cancelling orders,
 * viewing restaurant orders, and updating order status.
 * All tests run without a real server using MockMvc.
 */
@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtService jwtService; // ✅ ADDED (FIX)

    @MockBean
    private CustomUserDetailsService customUserDetailsService; // ✅ ADD THI

    private OrderResponseDto orderResponse;

    private static final String TOKEN = "Bearer mocked.jwt.token";
    private static final Long ORDER_ID = 100L;
    private static final Long RESTAURANT_ID = 10L;
    private static final Long ADDRESS_ID = 20L;

    /**
     * Builds a sample order response that is reused across multiple tests
     * so each test starts with consistent data.
     */
    @BeforeEach
    void setUp() {
        orderResponse = new OrderResponseDto(
                ORDER_ID,
                RESTAURANT_ID,
                "Spice Garden",
                List.of(),
                500.0,
                "PLACED",
                LocalDateTime.now(),
                "123 MG Road, Indore, MP - 452001"
        );
    }

    /**
     * Checks that placing an order with a valid token and address ID
     * returns a 200 response with the created order details.
     */
    @Test
    void placeOrder_shouldReturn200_whenRequestIsValid() throws Exception {
        when(orderService.placeOrder(eq(TOKEN), eq(ADDRESS_ID))).thenReturn(orderResponse);

        mockMvc.perform(post(OrderController.BASE_URL + OrderController.PLACE)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("deliveryAddressId", ADDRESS_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ORDER_ID))
                .andExpect(jsonPath("$.restaurantName").value("Spice Garden"))
                .andExpect(jsonPath("$.status").value("PLACED"));

        verify(orderService, times(1)).placeOrder(eq(TOKEN), eq(ADDRESS_ID));
    }

    /**
     * Checks that sending a place order request without a body
     * returns a 400 bad request response.
     */
    @Test
    void placeOrder_shouldReturn400_whenBodyIsMissing() throws Exception {
        mockMvc.perform(post(OrderController.BASE_URL + OrderController.PLACE)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Checks that sending a place order request without the Authorization header
     * returns a 400 bad request response.
     */
    @Test
    void placeOrder_shouldReturn400_whenTokenIsMissing() throws Exception {
        mockMvc.perform(post(OrderController.BASE_URL + OrderController.PLACE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("deliveryAddressId", ADDRESS_ID))))
                .andExpect(status().isBadRequest());
    }

    /**
     * Checks that fetching the current user's orders with a valid token
     * returns a 200 response containing the list of orders.
     */
    @Test
    void getMyOrders_shouldReturn200_withOrderList() throws Exception {
        when(orderService.getMyOrders(TOKEN)).thenReturn(List.of(orderResponse));

        mockMvc.perform(get(OrderController.BASE_URL + OrderController.MY_ORDERS)
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ORDER_ID))
                .andExpect(jsonPath("$[0].restaurantName").value("Spice Garden"));

        verify(orderService, times(1)).getMyOrders(TOKEN);
    }

    /**
     * Checks that fetching orders when none exist returns
     * a 200 response with an empty list.
     */
    @Test
    void getMyOrders_shouldReturn200_withEmptyList_whenNoOrders() throws Exception {
        when(orderService.getMyOrders(TOKEN)).thenReturn(List.of());

        mockMvc.perform(get(OrderController.BASE_URL + OrderController.MY_ORDERS)
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    /**
     * Checks that cancelling a valid order with a valid token
     * returns a 200 response with the cancelled order showing CANCELLED status.
     */
    @Test
    void cancelOrder_shouldReturn200_whenOrderIsCancelled() throws Exception {
        OrderResponseDto cancelled = new OrderResponseDto(
                ORDER_ID, RESTAURANT_ID, "Spice Garden",
                List.of(), 500.0, "CANCELLED",
                LocalDateTime.now(), "123 MG Road, Indore, MP - 452001"
        );

        when(orderService.cancelOrder(ORDER_ID, TOKEN)).thenReturn(cancelled);

        mockMvc.perform(delete(OrderController.BASE_URL + "/cancel/{orderId}", ORDER_ID)
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(orderService, times(1)).cancelOrder(ORDER_ID, TOKEN);
    }

    /**
     * Checks that cancelling an order without a token
     * returns a 400 bad request response.
     */
    @Test
    void cancelOrder_shouldReturn400_whenTokenIsMissing() throws Exception {
        mockMvc.perform(delete(OrderController.BASE_URL + "/cancel/{orderId}", ORDER_ID))
                .andExpect(status().isBadRequest());
    }

    /**
     * Checks that a restaurant owner fetching orders for their restaurant
     * with a valid token gets a 200 response with the order list.
     */
    @Test
    void getRestaurantOrders_shouldReturn200_withOrderList() throws Exception {
        when(orderService.getRestaurantOrders(RESTAURANT_ID, TOKEN)).thenReturn(List.of(orderResponse));

        mockMvc.perform(get(OrderController.BASE_URL + "/restaurant/{restaurantId}", RESTAURANT_ID)
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ORDER_ID))
                .andExpect(jsonPath("$[0].restaurantName").value("Spice Garden"));

        verify(orderService, times(1)).getRestaurantOrders(RESTAURANT_ID, TOKEN);
    }

    /**
     * Checks that fetching restaurant orders without a token
     * returns a 400 bad request response.
     */
    @Test
    void getRestaurantOrders_shouldReturn400_whenTokenIsMissing() throws Exception {
        mockMvc.perform(get(OrderController.BASE_URL + "/restaurant/{restaurantId}", RESTAURANT_ID))
                .andExpect(status().isBadRequest());
    }

    /**
     * Checks that updating an order status with a valid status string and token
     * returns a 200 response with the updated order.
     */
    @Test
    void updateStatus_shouldReturn200_whenStatusIsValid() throws Exception {
        OrderResponseDto updated = new OrderResponseDto(
                ORDER_ID, RESTAURANT_ID, "Spice Garden",
                List.of(), 500.0, "PENDING",
                LocalDateTime.now(), "123 MG Road, Indore, MP - 452001"
        );

        when(orderService.updateOrderStatus(ORDER_ID, "PENDING", TOKEN)).thenReturn(updated);

        mockMvc.perform(patch(OrderController.BASE_URL + "/status/{orderId}", ORDER_ID)
                        .header("Authorization", TOKEN)
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService, times(1)).updateOrderStatus(ORDER_ID, "PENDING", TOKEN);
    }

    /**
     * Checks that updating order status without providing the status param
     * returns a 400 bad request response.
     */
    @Test
    void updateStatus_shouldReturn400_whenStatusParamIsMissing() throws Exception {
        mockMvc.perform(patch(OrderController.BASE_URL + "/status/{orderId}", ORDER_ID)
                        .header("Authorization", TOKEN))
                .andExpect(status().isBadRequest());
    }

    /**
     * Checks that updating order status without a token
     * returns a 400 bad request response.
     */
    @Test
    void updateStatus_shouldReturn400_whenTokenIsMissing() throws Exception {
        mockMvc.perform(patch(OrderController.BASE_URL + "/status/{orderId}", ORDER_ID)
                        .param("status", "PENDING"))
                .andExpect(status().isBadRequest());
    }
}