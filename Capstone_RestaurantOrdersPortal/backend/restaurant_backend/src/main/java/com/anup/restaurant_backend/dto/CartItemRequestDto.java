// ============================================
// FILE 1: CartItemRequestDto.java
// ============================================
// What customer SENDS when adding item to cart
// Just two things: which item + how many

package com.anup.restaurant_backend.dto;

/**
 *  WHAT CUSTOMER SENDS:
 * {
 *   "menuItemId": 3,
 *   "quantity": 2
 * }
 */
public class CartItemRequestDto {

    /**
     * Which food item to add
     * Example: menuItemId=3 means "Paneer Pizza"
     */
    private Long menuItemId;

    /**
     * How many of this item
     * Example: quantity=2 means "2 Paneer Pizzas"
     */
    private Integer quantity;

    public CartItemRequestDto() {}

    public Long getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}