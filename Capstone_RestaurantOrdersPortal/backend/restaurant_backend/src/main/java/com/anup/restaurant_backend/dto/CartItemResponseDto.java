package com.anup.restaurant_backend.dto;

/**
 * Represents a single item inside the cart response.
 * It contains item details along with quantity and calculated subtotal.
 */
public class CartItemResponseDto {

    private Long cartItemId;
    private Long menuItemId;
    private String menuItemName;
    private Integer quantity;
    private Double price;
    private Double subtotal;

    /**
     * Default constructor.
     */
    public CartItemResponseDto() {}

    /**
     * Creates a cart item response with all required fields.
     */
    public CartItemResponseDto(Long cartItemId, Long menuItemId, String menuItemName,
                               Integer quantity, Double price, Double subtotal) {
        this.cartItemId = cartItemId;
        this.menuItemId = menuItemId;
        this.menuItemName = menuItemName;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
    }

    /**
     * Returns cart item ID.
     */
    public Long getCartItemId() { return cartItemId; }

    /**
     * Returns menu item ID.
     */
    public Long getMenuItemId() { return menuItemId; }

    /**
     * Returns name of the menu item.
     */
    public String getMenuItemName() { return menuItemName; }

    /**
     * Returns quantity of the item.
     */
    public Integer getQuantity() { return quantity; }

    /**
     * Returns price of a single item.
     */
    public Double getPrice() { return price; }

    /**
     * Returns total price for this item (price × quantity).
     */
    public Double getSubtotal() { return subtotal; }
}