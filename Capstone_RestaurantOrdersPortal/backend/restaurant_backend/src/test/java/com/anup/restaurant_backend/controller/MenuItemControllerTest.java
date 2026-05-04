package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.MenuItemRequestDto;
import com.anup.restaurant_backend.dto.MenuItemResponseDto;
import com.anup.restaurant_backend.service.MenuItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link MenuItemController}.
 * Tests controller method logic directly without starting a server,
 * making them fully compatible with Java 17.
 */
@ExtendWith(MockitoExtension.class)
class MenuItemControllerTest {

    @Mock
    private MenuItemService menuItemService;

    @InjectMocks
    private MenuItemController menuItemController;

    private MenuItemRequestDto requestDto;
    private MenuItemResponseDto responseDto;

    private static final String TOKEN = "Bearer mocked.jwt.token";
    private static final Long RESTAURANT_ID = 10L;
    private static final Long MENU_ITEM_ID = 100L;
    private static final Long CATEGORY_ID = 50L;

    /**
     * Builds shared request and response objects before each test runs.
     */
    @BeforeEach
    void setUp() {
        requestDto = new MenuItemRequestDto();
        requestDto.setName("Paneer Tikka");
        requestDto.setDescription("Grilled paneer");
        requestDto.setPrice(250.0);
        requestDto.setImageUrl("http://img.com/paneer.jpg");
        requestDto.setAvailable(true);
        requestDto.setCategoryId(CATEGORY_ID);

        responseDto = new MenuItemResponseDto(
                MENU_ITEM_ID, "Paneer Tikka", "Grilled paneer",
                250.0, "http://img.com/paneer.jpg", true,
                CATEGORY_ID, RESTAURANT_ID
        );
    }

    /**
     * Verifies that adding a menu item calls the service with the correct
     * arguments and returns the created item response.
     */
    @Test
    void addMenuItem_shouldReturnCreatedItem_whenCalledWithValidInput() {
        when(menuItemService.addMenuItem(RESTAURANT_ID, requestDto, TOKEN)).thenReturn(responseDto);

        MenuItemResponseDto result = menuItemController.addMenuItem(RESTAURANT_ID, requestDto, TOKEN);

        assertNotNull(result);
        assertEquals("Paneer Tikka", result.getName());
        assertEquals(250.0, result.getPrice());
        assertEquals(RESTAURANT_ID, result.getRestaurantId());
        verify(menuItemService, times(1)).addMenuItem(RESTAURANT_ID, requestDto, TOKEN);
    }

    /**
     * Verifies that fetching menu items returns the full list
     * from the service without any modification.
     */
    @Test
    void getMenuItems_shouldReturnList_whenItemsExist() {
        when(menuItemService.getMenuItemsByRestaurant(RESTAURANT_ID)).thenReturn(List.of(responseDto));

        List<MenuItemResponseDto> result = menuItemController.getMenuItems(RESTAURANT_ID);

        assertEquals(1, result.size());
        assertEquals("Paneer Tikka", result.get(0).getName());
        verify(menuItemService, times(1)).getMenuItemsByRestaurant(RESTAURANT_ID);
    }

    /**
     * Verifies that fetching menu items returns an empty list
     * when no items exist for the restaurant.
     */
    @Test
    void getMenuItems_shouldReturnEmptyList_whenNoItemsExist() {
        when(menuItemService.getMenuItemsByRestaurant(RESTAURANT_ID)).thenReturn(List.of());

        List<MenuItemResponseDto> result = menuItemController.getMenuItems(RESTAURANT_ID);

        assertTrue(result.isEmpty());
        verify(menuItemService, times(1)).getMenuItemsByRestaurant(RESTAURANT_ID);
    }

    /**
     * Verifies that updating a menu item passes the correct item ID to the service
     * and returns the updated item response.
     */
    @Test
    void updateMenuItem_shouldReturnUpdatedItem_whenCalledWithValidInput() {
        MenuItemResponseDto updated = new MenuItemResponseDto(
                MENU_ITEM_ID, "Paneer Tikka Masala", "Richer version",
                300.0, "http://img.com/ptm.jpg", true,
                CATEGORY_ID, RESTAURANT_ID
        );

        requestDto.setName("Paneer Tikka Masala");
        requestDto.setPrice(300.0);

        when(menuItemService.updateMenuItem(MENU_ITEM_ID, requestDto, TOKEN)).thenReturn(updated);

        MenuItemResponseDto result = menuItemController.updateMenuItem(
                RESTAURANT_ID, MENU_ITEM_ID, requestDto, TOKEN);

        assertEquals("Paneer Tikka Masala", result.getName());
        assertEquals(300.0, result.getPrice());
        verify(menuItemService, times(1)).updateMenuItem(MENU_ITEM_ID, requestDto, TOKEN);
    }

    /**
     * Verifies that deleting a menu item calls the service once with the correct
     * item ID and returns the expected success message.
     */
    @Test
    void deleteMenuItem_shouldReturnSuccessMessage_whenItemExists() {
        doNothing().when(menuItemService).deleteMenuItem(MENU_ITEM_ID, TOKEN);

        String result = menuItemController.deleteMenuItem(RESTAURANT_ID, MENU_ITEM_ID, TOKEN);

        assertEquals("Menu item deleted successfully", result);
        verify(menuItemService, times(1)).deleteMenuItem(MENU_ITEM_ID, TOKEN);
    }

    /**
     * Verifies that the controller passes the restaurant ID from the path
     * to the service correctly when adding an item.
     */
    @Test
    void addMenuItem_shouldPassRestaurantIdToService_fromPathVariable() {
        when(menuItemService.addMenuItem(eq(RESTAURANT_ID), any(MenuItemRequestDto.class), eq(TOKEN)))
                .thenReturn(responseDto);

        menuItemController.addMenuItem(RESTAURANT_ID, requestDto, TOKEN);

        verify(menuItemService).addMenuItem(eq(RESTAURANT_ID), any(MenuItemRequestDto.class), eq(TOKEN));
    }

    /**
     * Verifies that the controller passes the menu item ID from the path
     * to the service correctly when deleting, not the restaurant ID.
     */
    @Test
    void deleteMenuItem_shouldPassMenuItemIdToService_notRestaurantId() {
        doNothing().when(menuItemService).deleteMenuItem(MENU_ITEM_ID, TOKEN);

        menuItemController.deleteMenuItem(RESTAURANT_ID, MENU_ITEM_ID, TOKEN);

        verify(menuItemService).deleteMenuItem(MENU_ITEM_ID, TOKEN);
        verify(menuItemService, never()).deleteMenuItem(eq(RESTAURANT_ID), anyString());
    }
}