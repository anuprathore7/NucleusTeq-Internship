// ─────────────────────────────────────────
// FILE: OrderItemResponseDto.java
// ─────────────────────────────────────────
package com.anup.restaurant_backend.dto;

public class OrderItemResponseDto {
    private Long id;
    private String itemName;
    private Double price;
    private Integer quantity;
    private Double subtotal;

    public OrderItemResponseDto() {}

    public OrderItemResponseDto(Long id, String itemName, Double price,
                                Integer quantity, Double subtotal) {
        this.id = id;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public Long getId() { return id; }
    public String getItemName() { return itemName; }
    public Double getPrice() { return price; }
    public Integer getQuantity() { return quantity; }
    public Double getSubtotal() { return subtotal; }
}