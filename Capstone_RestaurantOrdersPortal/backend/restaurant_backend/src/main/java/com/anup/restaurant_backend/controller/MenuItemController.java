package com.anup.restaurant_backend.controller;
import com.anup.restaurant_backend.dto.MenuItemRequestDto;
import com.anup.restaurant_backend.dto.MenuItemResponseDto;
import com.anup.restaurant_backend.service.MenuItemService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * This controller manages menu items for a restaurant.
 * It allows owners to add, update, and delete items,
 * while customers can view the available menu items.
 */
@RestController
@RequestMapping(MenuItemController.BASE_URL)
public class MenuItemController {
    public static final String BASE_URL="/api/restaurants/{restaurantId}/menu-items";
    public static final String UPDATE="/{menuItemId}";
    private final MenuItemService menuItemService;
    public MenuItemController(MenuItemService menuItemService){this.menuItemService=menuItemService;}
    /**
     * Adds a new menu item to a restaurant.
     * This operation is allowed only for the restaurant owner.
     */
    @PostMapping
    public MenuItemResponseDto addMenuItem(@PathVariable Long restaurantId,@RequestBody MenuItemRequestDto request,@RequestHeader("Authorization") String token){
        return menuItemService.addMenuItem(restaurantId,request,token);
    }
    /**
     * Returns all menu items for a specific restaurant.
     * This is used by customers to browse food options.
     */
    @GetMapping
    public List<MenuItemResponseDto> getMenuItems(@PathVariable Long restaurantId){
        return menuItemService.getMenuItemsByRestaurant(restaurantId);
    }
    /**
     * Updates details of an existing menu item.
     * Only the restaurant owner can perform this action.
     */
    @PutMapping(UPDATE)
    public MenuItemResponseDto updateMenuItem(@PathVariable Long restaurantId,@PathVariable Long menuItemId,@RequestBody MenuItemRequestDto request,@RequestHeader("Authorization") String token){
        return menuItemService.updateMenuItem(menuItemId,request,token);
    }
    /**
     * Deletes a menu item from the restaurant.
     * This action is restricted to the owner.
     */
    @DeleteMapping(UPDATE)
    public String deleteMenuItem(@PathVariable Long restaurantId,@PathVariable Long menuItemId,@RequestHeader("Authorization") String token){
        menuItemService.deleteMenuItem(menuItemId,token);
        return "Menu item deleted successfully";
    }
}