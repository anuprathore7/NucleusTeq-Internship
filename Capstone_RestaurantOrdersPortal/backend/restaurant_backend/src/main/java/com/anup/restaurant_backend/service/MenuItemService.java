package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.MenuItemRequestDto;
import com.anup.restaurant_backend.dto.MenuItemResponseDto;

import java.util.List;

/**
 *   MenuItemService (Interface)
 */
public interface MenuItemService {

    /**
     * Owner adds a new food item
     * @param restaurantId  from URL
     * @param request       item details from body
     * @param token         JWT from Authorization header
     */
    MenuItemResponseDto addMenuItem(Long restaurantId, MenuItemRequestDto request, String token);

    /**
     * Anyone can view menu items of a restaurant
     * @param restaurantId  from URL
     */
    List<MenuItemResponseDto> getMenuItemsByRestaurant(Long restaurantId);

    /**
     * Owner updates an existing food item
     * @param menuItemId    from URL
     * @param request       updated details from body
     * @param token         JWT from Authorization header
     */
    MenuItemResponseDto updateMenuItem(Long menuItemId, MenuItemRequestDto request, String token);



    /**
     * Owner deletes a food item
     * @param menuItemId    from URL
     * @param token         JWT from Authorization header
     */
    void deleteMenuItem(Long menuItemId, String token);
}