package com.anup.restaurant_backend.entity;

import jakarta.persistence.*;

/**
 *  OrderItem Entity
 *
 *  Real-life meaning:
 * Each item inside an order
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    /**
     *  Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     *  MANY order items → belong to ONE order
     */
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    /**
     *  MANY order items → refer to ONE menu item
     */
    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    /**
     *  Quantity
     */
    private Integer quantity;

    /**
     *  Price (per item total)
     */
    private Double price;

    public OrderItem() {
    }

    // ================= GETTERS & SETTERS =================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    /**
     *  Links to order → order_id stored
     */
    public void setOrder(Order order) {
        this.order = order;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    /**
     *  Links to menu item
     */
    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public Integer getQuantity() {
        return quantity;
    }

    /**
     *  Quantity ordered
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    /**
     *  price = item price * quantity
     */
    public void setPrice(Double price) {
        this.price = price;
    }
}