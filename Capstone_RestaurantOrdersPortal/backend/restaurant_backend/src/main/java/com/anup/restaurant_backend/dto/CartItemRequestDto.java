package com.anup.restaurant_backend.dto;

/**
 * Request object used when a user adds an item to the cart.
 * It contains the menu item reference and the quantity selected by the user.
 */
public class CartItemRequestDto {

    private Long menuItemId;
    private Integer quantity;

    /**
     * Default constructor.
     */
    public CartItemRequestDto() {}

    /**
     * Returns menu item ID that user wants to add.
     */
    public Long getMenuItemId() { return menuItemId; }

    /**
     * Sets menu item ID.
     */
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }

    /**
     * Returns quantity of the item.
     */
    public Integer getQuantity() { return quantity; }

    /**
     * Sets quantity of the item.
     */
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}