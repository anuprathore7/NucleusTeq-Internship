package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.MenuItemRequestDto;
import com.anup.restaurant_backend.dto.MenuItemResponseDto;
import com.anup.restaurant_backend.service.MenuItemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================
 *   MenuItemController
 * ============================================
 */
@RestController
@RequestMapping("/api/restaurants/{restaurantId}/menu-items")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    /**
     * ADD MENU ITEM
     * POST /api/restaurants/5/menu-items
     * Needs token - OWNER only
     */
    @PostMapping
    public MenuItemResponseDto addMenuItem(
            @PathVariable Long restaurantId,
            @RequestBody MenuItemRequestDto request,
            @RequestHeader("Authorization") String token) {

        return menuItemService.addMenuItem(restaurantId, request, token);
    }

    /**
     * GET ALL MENU ITEMS OF A RESTAURANT
     * GET /api/restaurants/5/menu-items
     * Public - no token needed
     * Customer uses this to browse the menu
     */
    @GetMapping
    public List<MenuItemResponseDto> getMenuItems(
            @PathVariable Long restaurantId) {

        return menuItemService.getMenuItemsByRestaurant(restaurantId);
    }

    /**
     * UPDATE MENU ITEM
     * PUT /api/restaurants/5/menu-items/1
     * Needs token - OWNER only
     */
    @PutMapping("/{menuItemId}")
    public MenuItemResponseDto updateMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId,
            @RequestBody MenuItemRequestDto request,
            @RequestHeader("Authorization") String token) {

        return menuItemService.updateMenuItem(menuItemId, request, token);
    }

    /**
     * DELETE MENU ITEM
     * DELETE /api/restaurants/5/menu-items/1
     * Needs token - OWNER only
     */
    @DeleteMapping("/{menuItemId}")
    public String deleteMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long menuItemId,
            @RequestHeader("Authorization") String token) {

        menuItemService.deleteMenuItem(menuItemId, token);
        return "Menu item deleted successfully";
    }
}