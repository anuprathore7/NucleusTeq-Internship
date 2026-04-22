package com.anup.restaurant_backend.entity;

import jakarta.persistence.*;
import java.util.List;

/**
 *  Cart Entity
 *
 *  Real-life meaning:
 * This is the user's shopping cart
 * → where selected food items are stored before placing order
 *
 * Example:
 * User adds:
 * → Pizza
 * → Coke
 * → Burger
 *
 * All go inside Cart
 */
@Entity
@Table(name = "carts")
public class Cart {

    /**
     *  Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     *  ONE cart belongs to ONE user
     *
     * Each user has only ONE cart
     */
    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /**
     *  ONE cart has MANY cart items
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    private List<CartItem> items;

    /**
     *  Total price of all items in cart
     */
    private Double totalAmount;

    /**
     *  Default constructor
     */
    public Cart() {
    }

    // ================= GETTERS & SETTERS =================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    /**
     *  When user is set:
     * → user_id is stored in DB
     */
    public void setUser(UserEntity user) {
        this.user = user;
    }

    public List<CartItem> getItems() {
        return items;
    }

    /**
     *  Holds all selected items
     */
    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    /**
     *  Updated when:
     * → item added
     * → item removed
     */
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
}