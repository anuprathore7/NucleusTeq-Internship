package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.MenuItemRequestDto;
import com.anup.restaurant_backend.dto.MenuItemResponseDto;
import com.anup.restaurant_backend.entity.Category;
import com.anup.restaurant_backend.entity.MenuItem;
import com.anup.restaurant_backend.entity.Restaurant;
import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.enums.UserRole;
import com.anup.restaurant_backend.exception.ResourceNotFoundException;
import com.anup.restaurant_backend.repository.CategoryRepository;
import com.anup.restaurant_backend.repository.MenuItemRepository;
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
 * Unit tests for {@link MenuItemServiceImpl}.
 * Covers add, fetch, update, and delete menu item operations
 * including ownership and category-restaurant validation checks.
 */
@ExtendWith(MockitoExtension.class)
class MenuItemServiceImplTest {

    @Mock private MenuItemRepository menuItemRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;

    @InjectMocks
    private MenuItemServiceImpl menuItemService;

    private UserEntity owner;
    private UserEntity otherUser;
    private Restaurant restaurant;
    private Category category;
    private MenuItem menuItem;
    private MenuItemRequestDto requestDto;

    private static final String TOKEN = "Bearer mocked.jwt.token";
    private static final String EMAIL = "owner@test.com";
    private static final Long RESTAURANT_ID = 10L;
    private static final Long CATEGORY_ID = 50L;
    private static final Long MENU_ITEM_ID = 100L;

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

        restaurant = new Restaurant();
        restaurant.setId(RESTAURANT_ID);
        restaurant.setOwner(owner);

        category = new Category();
        category.setId(CATEGORY_ID);
        category.setName("Starters");
        category.setRestaurant(restaurant);

        menuItem = new MenuItem();
        menuItem.setId(MENU_ITEM_ID);
        menuItem.setName("Paneer Tikka");
        menuItem.setDescription("Grilled paneer");
        menuItem.setPrice(250.0);
        menuItem.setImageUrl("http://img.com/paneer.jpg");
        menuItem.setAvailable(true);
        menuItem.setCategory(category);
        menuItem.setRestaurant(restaurant);

        requestDto = new MenuItemRequestDto();
        requestDto.setName("Paneer Tikka");
        requestDto.setDescription("Grilled paneer");
        requestDto.setPrice(250.0);
        requestDto.setImageUrl("http://img.com/paneer.jpg");
        requestDto.setAvailable(true);
        requestDto.setCategoryId(CATEGORY_ID);
    }

    /**
     * Verifies that a menu item is saved successfully
     * when the owner is valid and the category belongs to the restaurant.
     */
    @Test
    void addMenuItem_shouldSucceed_whenOwnerAndCategoryAreValid() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);

        MenuItemResponseDto result = menuItemService.addMenuItem(RESTAURANT_ID, requestDto, TOKEN);

        assertNotNull(result);
        assertEquals("Paneer Tikka", result.getName());
        assertEquals(250.0, result.getPrice());
        assertEquals(RESTAURANT_ID, result.getRestaurantId());
        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }

    /**
     * Verifies that {@link RuntimeException} is thrown
     * when the requesting user is not the restaurant owner.
     */
    @Test
    void addMenuItem_shouldThrow_whenUserIsNotOwner() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn("other@test.com");
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));

        assertThrows(RuntimeException.class,
                () -> menuItemService.addMenuItem(RESTAURANT_ID, requestDto, TOKEN));

        verify(menuItemRepository, never()).save(any());
    }

    /**
     * Verifies that {@link ResourceNotFoundException} is thrown
     * when the restaurant does not exist.
     */
    @Test
    void addMenuItem_shouldThrow_whenRestaurantNotFound() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.addMenuItem(RESTAURANT_ID, requestDto, TOKEN));

        verify(menuItemRepository, never()).save(any());
    }

    /**
     * Verifies that {@link ResourceNotFoundException} is thrown
     * when the category does not exist.
     */
    @Test
    void addMenuItem_shouldThrow_whenCategoryNotFound() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.addMenuItem(RESTAURANT_ID, requestDto, TOKEN));

        verify(menuItemRepository, never()).save(any());
    }

    /**
     * Verifies that {@link RuntimeException} is thrown
     * when the category belongs to a different restaurant.
     */
    @Test
    void addMenuItem_shouldThrow_whenCategoryBelongsToDifferentRestaurant() {
        Restaurant otherRestaurant = new Restaurant();
        otherRestaurant.setId(99L);
        otherRestaurant.setOwner(owner);

        Category foreignCategory = new Category();
        foreignCategory.setId(CATEGORY_ID);
        foreignCategory.setRestaurant(otherRestaurant);

        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(foreignCategory));

        assertThrows(RuntimeException.class,
                () -> menuItemService.addMenuItem(RESTAURANT_ID, requestDto, TOKEN));

        verify(menuItemRepository, never()).save(any());
    }

    /**
     * Verifies that available defaults to true
     * when the request does not specify it.
     */
    @Test
    void addMenuItem_shouldDefaultAvailableToTrue_whenNotProvided() {
        requestDto.setAvailable(null);

        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(restaurantRepository.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
        when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(invocation -> {
            MenuItem saved = invocation.getArgument(0);
            assertTrue(saved.getAvailable());
            return menuItem;
        });

        menuItemService.addMenuItem(RESTAURANT_ID, requestDto, TOKEN);
    }

    // ─────────────────────────────────────────
    // GET MENU ITEMS TESTS
    // ─────────────────────────────────────────

    /**
     * Verifies that all menu items for a restaurant are returned
     * when items exist.
     */
    @Test
    void getMenuItemsByRestaurant_shouldReturnList_whenItemsExist() {
        when(menuItemRepository.findByRestaurantId(RESTAURANT_ID)).thenReturn(List.of(menuItem));

        List<MenuItemResponseDto> result = menuItemService.getMenuItemsByRestaurant(RESTAURANT_ID);

        assertEquals(1, result.size());
        assertEquals("Paneer Tikka", result.get(0).getName());
        assertEquals(250.0, result.get(0).getPrice());
    }

    /**
     * Verifies that an empty list is returned
     * when no menu items exist for the restaurant.
     */
    @Test
    void getMenuItemsByRestaurant_shouldReturnEmptyList_whenNoItemsExist() {
        when(menuItemRepository.findByRestaurantId(RESTAURANT_ID)).thenReturn(List.of());

        List<MenuItemResponseDto> result = menuItemService.getMenuItemsByRestaurant(RESTAURANT_ID);

        assertTrue(result.isEmpty());
    }

    // ─────────────────────────────────────────
    // UPDATE MENU ITEM TESTS
    // ─────────────────────────────────────────

    /**
     * Verifies that a menu item is updated successfully
     * when the requesting user is the restaurant owner.
     */
    @Test
    void updateMenuItem_shouldSucceed_whenOwnerIsValid() {
        MenuItem updated = new MenuItem();
        updated.setId(MENU_ITEM_ID);
        updated.setName("Paneer Tikka Masala");
        updated.setDescription("Richer version");
        updated.setPrice(300.0);
        updated.setImageUrl("http://img.com/ptm.jpg");
        updated.setAvailable(true);
        updated.setCategory(category);
        updated.setRestaurant(restaurant);

        requestDto.setName("Paneer Tikka Masala");
        requestDto.setPrice(300.0);

        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(menuItemRepository.findById(MENU_ITEM_ID)).thenReturn(Optional.of(menuItem));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(updated);

        MenuItemResponseDto result = menuItemService.updateMenuItem(MENU_ITEM_ID, requestDto, TOKEN);

        assertEquals("Paneer Tikka Masala", result.getName());
        assertEquals(300.0, result.getPrice());
        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }

    /**
     * Verifies that {@link ResourceNotFoundException} is thrown
     * when the menu item to update does not exist.
     */
    @Test
    void updateMenuItem_shouldThrow_whenMenuItemNotFound() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(menuItemRepository.findById(MENU_ITEM_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.updateMenuItem(MENU_ITEM_ID, requestDto, TOKEN));

        verify(menuItemRepository, never()).save(any());
    }

    /**
     * Verifies that {@link RuntimeException} is thrown
     * when a user tries to update a menu item they do not own.
     */
    @Test
    void updateMenuItem_shouldThrow_whenUserIsNotOwner() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn("other@test.com");
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(menuItemRepository.findById(MENU_ITEM_ID)).thenReturn(Optional.of(menuItem));

        assertThrows(RuntimeException.class,
                () -> menuItemService.updateMenuItem(MENU_ITEM_ID, requestDto, TOKEN));

        verify(menuItemRepository, never()).save(any());
    }

    /**
     * Verifies that the category is updated on the menu item
     * when a new categoryId is provided in the request.
     */
    @Test
    void updateMenuItem_shouldUpdateCategory_whenNewCategoryIdProvided() {
        Category newCategory = new Category();
        newCategory.setId(77L);
        newCategory.setName("Mains");
        newCategory.setRestaurant(restaurant);

        requestDto.setCategoryId(77L);

        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(menuItemRepository.findById(MENU_ITEM_ID)).thenReturn(Optional.of(menuItem));
        when(categoryRepository.findById(77L)).thenReturn(Optional.of(newCategory));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(menuItem);

        menuItemService.updateMenuItem(MENU_ITEM_ID, requestDto, TOKEN);

        verify(categoryRepository, times(1)).findById(77L);
        verify(menuItemRepository, times(1)).save(any(MenuItem.class));
    }

    // ─────────────────────────────────────────
    // DELETE MENU ITEM TESTS
    // ─────────────────────────────────────────

    /**
     * Verifies that a menu item is deleted successfully
     * when the requesting user is the restaurant owner.
     */
    @Test
    void deleteMenuItem_shouldSucceed_whenOwnerIsValid() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(menuItemRepository.findById(MENU_ITEM_ID)).thenReturn(Optional.of(menuItem));

        menuItemService.deleteMenuItem(MENU_ITEM_ID, TOKEN);

        verify(menuItemRepository, times(1)).deleteById(MENU_ITEM_ID);
    }

    /**
     * Verifies that {@link ResourceNotFoundException} is thrown
     * when the menu item to delete does not exist.
     */
    @Test
    void deleteMenuItem_shouldThrow_whenMenuItemNotFound() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(owner));
        when(menuItemRepository.findById(MENU_ITEM_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> menuItemService.deleteMenuItem(MENU_ITEM_ID, TOKEN));

        verify(menuItemRepository, never()).deleteById(any());
    }

    /**
     * Verifies that {@link RuntimeException} is thrown
     * when a user tries to delete a menu item they do not own.
     */
    @Test
    void deleteMenuItem_shouldThrow_whenUserIsNotOwner() {
        when(jwtService.extractEmail("mocked.jwt.token")).thenReturn("other@test.com");
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(menuItemRepository.findById(MENU_ITEM_ID)).thenReturn(Optional.of(menuItem));

        assertThrows(RuntimeException.class,
                () -> menuItemService.deleteMenuItem(MENU_ITEM_ID, TOKEN));

        verify(menuItemRepository, never()).deleteById(any());
    }
}