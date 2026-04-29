package com.anup.restaurant_backend.dto;

/**
 * ============================================
 *   CartItemResponseDto
 * ============================================
 */
public class CartItemResponseDto {

    private Long cartItemId;
    private Long menuItemId;
    private String menuItemName;
    private Integer quantity;
    private Double price;
    private Double subtotal; // price x quantity

    public CartItemResponseDto() {}

    public CartItemResponseDto(Long cartItemId, Long menuItemId, String menuItemName,
                               Integer quantity, Double price, Double subtotal) {
        this.cartItemId = cartItemId;
        this.menuItemId = menuItemId;
        this.menuItemName = menuItemName;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
    }

    public Long getCartItemId() { return cartItemId; }
    public Long getMenuItemId() { return menuItemId; }
    public String getMenuItemName() { return menuItemName; }
    public Integer getQuantity() { return quantity; }
    public Double getPrice() { return price; }
    public Double getSubtotal() { return subtotal; }
}