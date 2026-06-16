package com.anup.restaurant_backend.dto;

/**
 * Represents a single item inside an order.
 * Includes pricing and quantity details for that item.
 */
public class OrderItemResponseDto {

    private Long id;
    private String itemName;
    private Double price;
    private Integer quantity;
    private Double subtotal;

    /**
     * Default constructor.
     */
    public OrderItemResponseDto() {}

    /**
     * Creates order item response with all fields.
     */
    public OrderItemResponseDto(Long id, String itemName, Double price,
                                Integer quantity, Double subtotal) {
        this.id = id;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    /**
     * Returns item ID.
     */
    public Long getId() { return id; }

    /**
     * Returns item name.
     */
    public String getItemName() { return itemName; }

    /**
     * Returns price.
     */
    public Double getPrice() { return price; }

    /**
     * Returns quantity.
     */
    public Integer getQuantity() { return quantity; }

    /**
     * Returns subtotal.
     */
    public Double getSubtotal() { return subtotal; }
}