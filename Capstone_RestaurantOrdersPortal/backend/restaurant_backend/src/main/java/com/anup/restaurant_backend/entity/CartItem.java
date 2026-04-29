package com.anup.restaurant_backend.entity;

import jakarta.persistence.*;

/**
 *  CartItem Entity
 *
 *  Real-life meaning:
 * Represents each item inside cart
 *
 * Example:
 * Cart:
 * → Pizza x2
 * → Coke x1
 */
@Entity
@Table(name = "cart_items")
public class CartItem {

    /**
     *  Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     *  MANY cart items → belong to ONE cart
     */
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    /**
     *  MANY cart items → refer to ONE menu item
     */
    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    /**
     *  Quantity of item
     */
    private Integer quantity;

    /**
     * Unit price at the time of adding to cart
     * We store this separately because owner can change
     * menu item price later — cart price should not change
     * Subtotal (price x quantity) is calculated in service
     */
    private Double price;

    public CartItem() {
    }

    // ================= GETTERS & SETTERS =================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    /**
     *  Sets cart relation → cart_id stored
     */
    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    /**
     *  Links item → menu_item_id stored
     */
    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public Integer getQuantity() {
        return quantity;
    }

    /**
     *  How many items user selected
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    /**
     *  price = menuItem.price * quantity
     */
    public void setPrice(Double price) {
        this.price = price;
    }
}