package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.RestaurantRequestDto;
import com.anup.restaurant_backend.dto.RestaurantResponseDto;
import com.anup.restaurant_backend.security.CustomUserDetailsService;
import com.anup.restaurant_backend.security.JwtService;
import com.anup.restaurant_backend.service.RestaurantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link RestaurantController}.
 *
 * We use {@code @WebMvcTest} to load only the web layer (no DB, no full Spring context).
 * {@code @AutoConfigureMockMvc(addFilters = false)} skips JWT security filters
 * so we can test controller logic without a real token.
 * Every service call is mocked using {@code @MockBean}.
 */
@WebMvcTest(RestaurantController.class)
@AutoConfigureMockMvc(addFilters = false)
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestaurantService restaurantService;


    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private static final String TOKEN = "Bearer mocked.jwt.token";
    private static final Long    REST_ID = 1L;

    private RestaurantRequestDto  requestDto;
    private RestaurantResponseDto responseDto;

    /**
     * Sets up a reusable request and response DTO before each test,
     * so individual tests stay focused on the scenario they cover.
     */
    @BeforeEach
    void setUp() {
        requestDto = new RestaurantRequestDto();
        requestDto.setName("Pizza Hub");
        requestDto.setDescription("Best pizza in town");
        requestDto.setAddress("Delhi");
        requestDto.setPhone("9876543210");

        responseDto = new RestaurantResponseDto(
                REST_ID,
                "Pizza Hub",
                "Best pizza in town",
                "Delhi",
                "9876543210",
                7L,
                "ACTIVE" // or null if you don’t care
        );
    }


    /**
     * Verifies that a valid POST request with body and token
     * creates a restaurant and returns 200 with the saved data.
     */
    @Test
    @DisplayName("createRestaurant — 200 when request is valid")
    void createRestaurant_validRequest_returns200() throws Exception {
        when(restaurantService.createRestaurant(any(RestaurantRequestDto.class), eq(TOKEN)))
                .thenReturn(responseDto);

        mockMvc.perform(post(RestaurantController.BASE_URL)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(REST_ID))
                .andExpect(jsonPath("$.name").value("Pizza Hub"))
                .andExpect(jsonPath("$.address").value("Delhi"));

        verify(restaurantService, times(1))
                .createRestaurant(any(RestaurantRequestDto.class), eq(TOKEN));
    }

    /**
     * Verifies that a POST request without the Authorization header
     * returns 400 Bad Request because the token is missing.
     */
    @Test
    @DisplayName("createRestaurant — 400 when token is missing")
    void createRestaurant_missingToken_returns400() throws Exception {
        mockMvc.perform(post(RestaurantController.BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that a POST request without a body returns 400.
     */
    @Test
    @DisplayName("createRestaurant — 400 when body is missing")
    void createRestaurant_missingBody_returns400() throws Exception {
        mockMvc.perform(post(RestaurantController.BASE_URL)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }



    /**
     * Verifies that GET /api/restaurants returns 200
     * and a list of all restaurants without needing a token.
     */
    @Test
    @DisplayName("getAll — 200 with list of restaurants")
    void getAll_returns200_withRestaurantList() throws Exception {
        when(restaurantService.getAllRestaurants()).thenReturn(List.of(responseDto));

        mockMvc.perform(get(RestaurantController.BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(REST_ID))
                .andExpect(jsonPath("$[0].name").value("Pizza Hub"));

        verify(restaurantService, times(1)).getAllRestaurants();
    }

    /**
     * Verifies that when no restaurants exist,
     * GET /api/restaurants returns 200 with an empty list.
     */
    @Test
    @DisplayName("getAll — 200 with empty list when no restaurants")
    void getAll_returnsEmptyList_whenNoneExist() throws Exception {
        when(restaurantService.getAllRestaurants()).thenReturn(List.of());

        mockMvc.perform(get(RestaurantController.BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }


    /**
     * Verifies that an owner can fetch their own restaurants
     * using GET /api/restaurants/owner with a valid token.
     */
    @Test
    @DisplayName("getMyRestaurants — 200 with owner's restaurants")
    void getMyRestaurants_validToken_returns200() throws Exception {
        when(restaurantService.getRestaurantsByOwnerToken(TOKEN))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get(RestaurantController.BASE_URL + RestaurantController.OWNER)
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Pizza Hub"));

        verify(restaurantService, times(1)).getRestaurantsByOwnerToken(TOKEN);
    }

    /**
     * Verifies that GET /api/restaurants/owner without a token returns 400.
     */
    @Test
    @DisplayName("getMyRestaurants — 400 when token missing")
    void getMyRestaurants_missingToken_returns400() throws Exception {
        mockMvc.perform(get(RestaurantController.BASE_URL + RestaurantController.OWNER))
                .andExpect(status().isBadRequest());
    }


    /**
     * Verifies that a valid restaurant ID returns 200 with restaurant details.
     */
    @Test
    @DisplayName("getById — 200 with restaurant details")
    void getById_validId_returns200() throws Exception {
        when(restaurantService.getRestaurantById(REST_ID)).thenReturn(responseDto);

        mockMvc.perform(get(RestaurantController.BASE_URL + "/" + REST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(REST_ID))
                .andExpect(jsonPath("$.name").value("Pizza Hub"));

        verify(restaurantService, times(1)).getRestaurantById(REST_ID);
    }



    /**
     * Verifies that a valid PUT request updates the restaurant
     * and returns 200 with updated data.
     */
    @Test
    @DisplayName("updateRestaurant — 200 when request is valid")
    void updateRestaurant_validRequest_returns200() throws Exception {
        when(restaurantService.updateRestaurant(eq(REST_ID), any(RestaurantRequestDto.class), eq(TOKEN)))
                .thenReturn(responseDto);

        mockMvc.perform(put(RestaurantController.BASE_URL + "/" + REST_ID)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pizza Hub"));

        verify(restaurantService, times(1))
                .updateRestaurant(eq(REST_ID), any(RestaurantRequestDto.class), eq(TOKEN));
    }

    /**
     * Verifies that PUT without a token returns 400.
     */
    @Test
    @DisplayName("updateRestaurant — 400 when token missing")
    void updateRestaurant_missingToken_returns400() throws Exception {
        mockMvc.perform(put(RestaurantController.BASE_URL + "/" + REST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }


    /**
     * Verifies that a valid DELETE request removes the restaurant
     * and returns a success message.
     */
    @Test
    @DisplayName("deleteRestaurant — 200 with success message")
    void deleteRestaurant_validRequest_returns200() throws Exception {
        doNothing().when(restaurantService).deleteRestaurant(REST_ID, TOKEN);

        mockMvc.perform(delete(RestaurantController.BASE_URL + "/" + REST_ID)
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("Restaurant deleted successfully"));

        verify(restaurantService, times(1)).deleteRestaurant(REST_ID, TOKEN);
    }

    /**
     * Verifies that DELETE without a token returns 400.
     */
    @Test
    @DisplayName("deleteRestaurant — 400 when token missing")
    void deleteRestaurant_missingToken_returns400() throws Exception {
        mockMvc.perform(delete(RestaurantController.BASE_URL + "/" + REST_ID))
                .andExpect(status().isBadRequest());
    }
}