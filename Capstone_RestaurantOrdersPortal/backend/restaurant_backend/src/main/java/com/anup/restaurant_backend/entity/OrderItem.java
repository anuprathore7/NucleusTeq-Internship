package com.anup.restaurant_backend.entity;

import jakarta.persistence.*;

/**
 * ============================================
 *   OrderItem Entity
 * ============================================
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Which order this item belongs to
     */
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    /**
     * Reference to original menu item
     * (kept for reference, not for price lookup)
     */
    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    /**
     * Snapshot of item name at time of ordering
     */
    private String itemName;

    /**
     * Snapshot of price at time of ordering
     */
    private Double price;

    /**
     * How many of this item were ordered
     */
    private Integer quantity;

    public OrderItem() {}

    // ── GETTERS & SETTERS ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public MenuItem getMenuItem() { return menuItem; }
    public void setMenuItem(MenuItem menuItem) { this.menuItem = menuItem; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}