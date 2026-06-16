package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.RestaurantRequestDto;
import com.anup.restaurant_backend.dto.RestaurantResponseDto;
import com.anup.restaurant_backend.entity.Restaurant;
import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.enums.UserRole;
import com.anup.restaurant_backend.exception.ResourceNotFoundException;
import com.anup.restaurant_backend.repository.RestaurantRepository;
import com.anup.restaurant_backend.repository.UserRepository;
import com.anup.restaurant_backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RestaurantServiceImpl}.
 * Covers create, fetch, update, delete, and image update operations
 * including ownership validation and role-based access checks.
 */
@ExtendWith(MockitoExtension.class)
class RestaurantServiceImplTest {

    @Mock private RestaurantRepository restaurantRepository;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    private UserEntity owner;
    private UserEntity otherUser;
    private UserEntity customer;
    private Restaurant restaurant;
    private RestaurantRequestDto requestDto;

    private static final String TOKEN = "Bearer mocked.jwt.token";
    private static final String EMAIL = "owner@test.com";
    private static final Long RESTAURANT_ID = 10L;

    /**
     * Initialises shared test data before each test method.
     */
    @BeforeEach
    void setUp() {
        owner = new UserEntity();
        owner.setId(1L);
        owner.setEmail(EMAIL);
        owner.setRole(UserRole.RESTAURANT_OWNER);

        otherUser = new UserEntity();
        otherUser.setId(2L);
        otherUser.setEmail("other@test.com");
        otherUser.setRole(UserRole.RESTAURANT_OWNER);

        customer = new UserEntity();
        customer.setId(3L);
        customer.setEmail("customer@test.com");
        customer.setRole(UserRole.USER);

        restaurant = new Restaurant();
        restaurant.setId(RESTAURANT_ID);
        restaurant.setName("Spice Garden");
        restaurant.setDescription("Indian cuisine");
        restaurant.setAddress("123 MG Road");
        restaurant.setPhone("9876543210");
        restaurant.setOwner(owner);

        requestDto = new RestaurantRequestDto();
        requestDto.setName("Spice Garden");
        requestDto.setDescription("Indian cuisine");
        requestDto.setAddress("123 MG Road");
        requestDto.setPhone("9876543210");
    }

    // ─────────────────────────────────────────
    // CREATE TESTS
    // ─────────────────────────────────────────

    /**
     * Verifies that a restaurant is created and saved successfully
     * when the requesting user has the RESTAURANT_OWNER role.
     */
    @Test
    void createRestaurant_shouldSucceed_whenUserIsOwner() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(restaurant);

        RestaurantResponseDto result = restaurantService.createRestaurant(requestDto, TOKEN);

        assertNotNull(result);
        assertEquals("Spice Garden", result.getName());
        assertEquals(RESTAURANT_ID, result.getId());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    /**
     * Verifies that {@link RuntimeException} is thrown
     * when a CUSTOMER tries to create a restaurant.
     */
    @Test
    void createRestaurant_shouldThrow_whenUserIsNotOwnerRole() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn("customer@test.com");
        when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));

        assertThrows(RuntimeException.class,
                () -> restaurantService.createRestaurant(requestDto, TOKEN));

        verify(restaurantRepository, never()).save(any());
    }

    /**
     * Verifies that {@link RuntimeException} is thrown
     * when the user extracted from the token does not exist in the database.
     */
    @Test
    void createRestaurant_shouldThrow_whenUserNotFound() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> restaurantService.createRestaurant(requestDto, TOKEN));

        verify(restaurantRepository, never()).save(any());
    }

    /**
     * Verifies that the owner is correctly linked to the restaurant
     * when it is created.
     */
    @Test
    void createRestaurant_shouldLinkOwnerToRestaurant() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> {
            Restaurant saved = invocation.getArgument(0);
            assertEquals(owner.getId(), saved.getOwner().getId());
            return restaurant;
        });

        restaurantService.createRestaurant(requestDto, TOKEN);
    }

    // ─────────────────────────────────────────
    // GET ALL TESTS
    // ─────────────────────────────────────────

    /**
     * Verifies that all restaurants are returned as a list
     * when restaurants exist in the database.
     */
    @Test
    void getAllRestaurants_shouldReturnList_whenRestaurantsExist() {
        when(restaurantRepository.findAll()).thenReturn(List.of(restaurant));

        List<RestaurantResponseDto> result = restaurantService.getAllRestaurants();

        assertEquals(1, result.size());
        assertEquals("Spice Garden", result.get(0).getName());
    }

    /**
     * Verifies that an empty list is returned
     * when no restaurants exist in the database.
     */
    @Test
    void getAllRestaurants_shouldReturnEmptyList_whenNoneExist() {
        when(restaurantRepository.findAll()).thenReturn(List.of());

        List<RestaurantResponseDto> result = restaurantService.getAllRestaurants();

        assertTrue(result.isEmpty());
    }

    // ─────────────────────────────────────────
    // GET BY ID TESTS
    // ─────────────────────────────────────────

    /**
     * Verifies that a restaurant is returned correctly
     * when the given ID exists.
     */
    @Test
    void getRestaurantById_shouldReturnRestaurant_whenFound() {
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));

        RestaurantResponseDto result = restaurantService.getRestaurantById(RESTAURANT_ID);

        assertNotNull(result);
        assertEquals(RESTAURANT_ID, result.getId());
        assertEquals("Spice Garden", result.getName());
    }

    /**
     * Verifies that {@link ResourceNotFoundException} is thrown
     * when the restaurant ID does not exist.
     */
    @Test
    void getRestaurantById_shouldThrow_whenNotFound() {
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> restaurantService.getRestaurantById(RESTAURANT_ID));
    }

    // ─────────────────────────────────────────
    // GET BY OWNER TOKEN TESTS
    // ─────────────────────────────────────────

    /**
     * Verifies that only restaurants belonging to the token owner are returned.
     */
    @Test
    void getRestaurantsByOwnerToken_shouldReturnOwnerRestaurants() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(restaurantRepository.findByOwnerId(owner.getId())).thenReturn(List.of(restaurant));

        List<RestaurantResponseDto> result = restaurantService.getRestaurantsByOwnerToken(TOKEN);

        assertEquals(1, result.size());
        assertEquals("Spice Garden", result.get(0).getName());
        verify(restaurantRepository, times(1)).findByOwnerId(owner.getId());
    }

    /**
     * Verifies that an empty list is returned
     * when the owner has no restaurants.
     */
    @Test
    void getRestaurantsByOwnerToken_shouldReturnEmptyList_whenOwnerHasNone() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(restaurantRepository.findByOwnerId(owner.getId())).thenReturn(List.of());

        List<RestaurantResponseDto> result = restaurantService.getRestaurantsByOwnerToken(TOKEN);

        assertTrue(result.isEmpty());
    }

    // ─────────────────────────────────────────
    // UPDATE TESTS
    // ─────────────────────────────────────────

    /**
     * Verifies that a restaurant is updated successfully
     * when the requesting user is the owner.
     */
    @Test
    void updateRestaurant_shouldSucceed_whenOwnerIsValid() {
        Restaurant updated = new Restaurant();
        updated.setId(RESTAURANT_ID);
        updated.setName("Spice Garden Plus");
        updated.setDescription("Updated description");
        updated.setAddress("456 New Road");
        updated.setPhone("9999999999");
        updated.setOwner(owner);

        requestDto.setName("Spice Garden Plus");

        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(updated);

        RestaurantResponseDto result = restaurantService.updateRestaurant(RESTAURANT_ID, requestDto, TOKEN);

        assertEquals("Spice Garden Plus", result.getName());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    /**
     * Verifies that {@link RuntimeException} is thrown
     * when a user tries to update a restaurant they do not own.
     */
    @Test
    void updateRestaurant_shouldThrow_whenUserIsNotOwner() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn("other@test.com");
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));

        assertThrows(RuntimeException.class,
                () -> restaurantService.updateRestaurant(RESTAURANT_ID, requestDto, TOKEN));

        verify(restaurantRepository, never()).save(any());
    }

    /**
     * Verifies that {@link ResourceNotFoundException} is thrown
     * when the restaurant to update does not exist.
     */
    @Test
    void updateRestaurant_shouldThrow_whenRestaurantNotFound() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> restaurantService.updateRestaurant(RESTAURANT_ID, requestDto, TOKEN));

        verify(restaurantRepository, never()).save(any());
    }

    // ─────────────────────────────────────────
    // DELETE TESTS
    // ─────────────────────────────────────────

    /**
     * Verifies that a restaurant is deleted successfully
     * when the requesting user is the owner.
     */
    @Test
    void deleteRestaurant_shouldSucceed_whenOwnerIsValid() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));

        restaurantService.deleteRestaurant(RESTAURANT_ID, TOKEN);

        verify(restaurantRepository, times(1)).deleteById(RESTAURANT_ID);
    }

    /**
     * Verifies that {@link RuntimeException} is thrown
     * when a user tries to delete a restaurant they do not own.
     */
    @Test
    void deleteRestaurant_shouldThrow_whenUserIsNotOwner() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn("other@test.com");
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));

        assertThrows(RuntimeException.class,
                () -> restaurantService.deleteRestaurant(RESTAURANT_ID, TOKEN));

        verify(restaurantRepository, never()).deleteById(any());
    }

    /**
     * Verifies that {@link ResourceNotFoundException} is thrown
     * when the restaurant to delete does not exist.
     */
    @Test
    void deleteRestaurant_shouldThrow_whenRestaurantNotFound() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> restaurantService.deleteRestaurant(RESTAURANT_ID, TOKEN));

        verify(restaurantRepository, never()).deleteById(any());
    }

    // ─────────────────────────────────────────
    // UPDATE IMAGE TESTS
    // ─────────────────────────────────────────

    /**
     * Verifies that the image path is saved to the restaurant
     * when the requesting user is the owner.
     */
    @Test
    void updateRestaurantImage_shouldSucceed_whenOwnerIsValid() {
        String imagePath = "/images/restaurant_10_uuid.jpg";

        Restaurant withImage = new Restaurant();
        withImage.setId(RESTAURANT_ID);
        withImage.setName("Spice Garden");
        withImage.setOwner(owner);
        withImage.setImagePath(imagePath);

        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(withImage);

        RestaurantResponseDto result = restaurantService.updateRestaurantImage(RESTAURANT_ID, imagePath, TOKEN);

        assertEquals(imagePath, result.getImagePath());
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    /**
     * Verifies that {@link RuntimeException} is thrown
     * when a user tries to update the image of a restaurant they do not own.
     */
    @Test
    void updateRestaurantImage_shouldThrow_whenUserIsNotOwner() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn("other@test.com");
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));

        assertThrows(RuntimeException.class,
                () -> restaurantService.updateRestaurantImage(RESTAURANT_ID, "/images/test.jpg", TOKEN));

        verify(restaurantRepository, never()).save(any());
    }
}