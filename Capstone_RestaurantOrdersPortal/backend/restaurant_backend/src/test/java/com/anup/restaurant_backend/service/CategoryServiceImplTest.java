package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.CategoryRequestDto;
import com.anup.restaurant_backend.dto.CategoryResponseDto;
import com.anup.restaurant_backend.entity.Category;
import com.anup.restaurant_backend.entity.Restaurant;
import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.enums.UserRole;
import com.anup.restaurant_backend.exception.ResourceNotFoundException;
import com.anup.restaurant_backend.repository.CategoryRepository;
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
 * Unit tests for {@link CategoryServiceImpl}.
 * Verifies add, fetch, update, and delete category operations
 * including ownership checks and error handling.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private UserEntity owner;
    private UserEntity otherUser;
    private Restaurant restaurant;
    private Category category;
    private CategoryRequestDto requestDto;

    private static final String TOKEN = "Bearer mocked.jwt.token";
    private static final String EMAIL = "owner@test.com";

    /**
     * Sets up reusable test data before each test method runs.
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

        restaurant = new Restaurant();
        restaurant.setId(10L);
        restaurant.setOwner(owner);

        category = new Category();
        category.setId(100L);
        category.setName("Starters");
        category.setRestaurant(restaurant);

        requestDto = new CategoryRequestDto();
        requestDto.setName("Starters");
    }

    /**
     * Verifies that a category is saved successfully
     * when the requesting user is the restaurant owner.
     */
    @Test
    void addCategory_shouldSucceed_whenOwnerIsValid() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(restaurantRepository.findById(10L)).thenReturn(Optional.of(restaurant));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponseDto result = categoryService.addCategory(10L, requestDto, TOKEN);

        assertNotNull(result);
        assertEquals("Starters", result.getName());
        assertEquals(10L, result.getRestaurantId());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    /**
     * Verifies that {@link ResourceNotFoundException} is thrown
     * when the restaurant does not exist.
     */
    @Test
    void addCategory_shouldThrow_whenRestaurantNotFound() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.addCategory(99L, requestDto, TOKEN));

        verify(categoryRepository, never()).save(any());
    }

    /**
     * Verifies that {@link RuntimeException} is thrown
     * when a user tries to add a category to a restaurant they do not own.
     */
    @Test
    void addCategory_shouldThrow_whenUserIsNotOwner() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn("other@test.com");
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(restaurantRepository.findById(10L)).thenReturn(Optional.of(restaurant));

        assertThrows(RuntimeException.class,
                () -> categoryService.addCategory(10L, requestDto, TOKEN));

        verify(categoryRepository, never()).save(any());
    }

    /**
     * Verifies that {@link RuntimeException} is thrown
     * when the user extracted from the token does not exist in the database.
     */
    @Test
    void addCategory_shouldThrow_whenUserNotFound() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> categoryService.addCategory(10L, requestDto, TOKEN));
    }

    // ─────────────────────────────────────────
    // GET CATEGORIES TESTS
    // ─────────────────────────────────────────

    /**
     * Verifies that all categories for a given restaurant are returned correctly.
     */
    @Test
    void getCategoriesByRestaurant_shouldReturnList_whenCategoriesExist() {
        when(categoryRepository.findByRestaurantId(10L)).thenReturn(List.of(category));

        List<CategoryResponseDto> result = categoryService.getCategoriesByRestaurant(10L);

        assertEquals(1, result.size());
        assertEquals("Starters", result.get(0).getName());
    }

    /**
     * Verifies that an empty list is returned
     * when no categories exist for the restaurant.
     */
    @Test
    void getCategoriesByRestaurant_shouldReturnEmptyList_whenNoCategoriesExist() {
        when(categoryRepository.findByRestaurantId(10L)).thenReturn(List.of());

        List<CategoryResponseDto> result = categoryService.getCategoriesByRestaurant(10L);

        assertTrue(result.isEmpty());
    }

    // ─────────────────────────────────────────
    // UPDATE CATEGORY TESTS
    // ─────────────────────────────────────────

    /**
     * Verifies that a category name is updated successfully
     * when the requesting user owns the restaurant.
     */
    @Test
    void updateCategory_shouldSucceed_whenOwnerIsValid() {
        Category updated = new Category();
        updated.setId(100L);
        updated.setName("Appetizers");
        updated.setRestaurant(restaurant);

        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(updated);

        requestDto.setName("Appetizers");
        CategoryResponseDto result = categoryService.updateCategory(100L, requestDto, TOKEN);

        assertEquals("Appetizers", result.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    /**
     * Verifies that {@link ResourceNotFoundException} is thrown
     * when the category to update does not exist.
     */
    @Test
    void updateCategory_shouldThrow_whenCategoryNotFound() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory(999L, requestDto, TOKEN));

        verify(categoryRepository, never()).save(any());
    }

    /**
     * Verifies that {@link RuntimeException} is thrown
     * when a user tries to update a category they do not own.
     */
    @Test
    void updateCategory_shouldThrow_whenUserIsNotOwner() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn("other@test.com");
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));

        assertThrows(RuntimeException.class,
                () -> categoryService.updateCategory(100L, requestDto, TOKEN));

        verify(categoryRepository, never()).save(any());
    }

    // ─────────────────────────────────────────
    // DELETE CATEGORY TESTS
    // ─────────────────────────────────────────

    /**
     * Verifies that a category is deleted successfully
     * when the requesting user owns the restaurant.
     */
    @Test
    void deleteCategory_shouldSucceed_whenOwnerIsValid() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));

        categoryService.deleteCategory(100L, TOKEN);

        verify(categoryRepository, times(1)).deleteById(100L);
    }

    /**
     * Verifies that {@link ResourceNotFoundException} is thrown
     * when the category to delete does not exist.
     */
    @Test
    void deleteCategory_shouldThrow_whenCategoryNotFound() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(999L, TOKEN));

        verify(categoryRepository, never()).deleteById(any());
    }

    /**
     * Verifies that {@link RuntimeException} is thrown
     * when a user tries to delete a category they do not own.
     */
    @Test
    void deleteCategory_shouldThrow_whenUserIsNotOwner() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn("other@test.com");
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(categoryRepository.findById(100L)).thenReturn(Optional.of(category));

        assertThrows(RuntimeException.class,
                () -> categoryService.deleteCategory(100L, TOKEN));

        verify(categoryRepository, never()).deleteById(any());
    }
}