package com.anup.restaurant_backend.entity;

import jakarta.persistence.*;

/**
 * MenuItem Entity
 *
 * This class represents actual food items in a restaurant
 *
 *  Real-life meaning:
 * These are the items customers will order
 * → Pizza
 * → Burger
 * → Cold Drink
 */
@Entity
@Table(name = "menu_items")
public class MenuItem {

    /**
     *  Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     *  Item Name
     * Example: Veg Pizza, Coke, Burger
     */
    private String name;

    /**
     *  Description of item
     */
    private String description;

    /**
     *  Price of item
     */
    private Double price;

    /**
     *  Image URL (optional, for frontend later)
     */
    private String imageUrl;

    /**
     *  Availability
     * true = available
     * false = out of stock
     */
    @Column(columnDefinition = "boolean default true")
    private Boolean available = true;

    /**
     *  RELATIONSHIP WITH CATEGORY
     *
     * MANY items → belong to ONE category
     */
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     *  RELATIONSHIP WITH RESTAURANT
     *
     * MANY items → belong to ONE restaurant
     *
     *  This helps directly fetch items by restaurant
     */
    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    /**
     *  Default constructor (required by JPA)
     */
    public MenuItem() {
    }

    // ================= GETTERS & SETTERS =================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    /**
     *  Flow:
     * Setting name → stored in DB
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    /**
     *  Price is used in:
     * → cart
     * → order
     * → wallet deduction
     */
    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getAvailable() {
        return available;
    }

    /**
     *    Controls:
     * → whether user can order or not
     */
    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Category getCategory() {
        return category;
    }

    /**
     *   MOST IMPORTANT
     *
     * When we set category:
     * → category_id stored in DB
     */
    public void setCategory(Category category) {
        this.category = category;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    /**
     *   ALSO IMPORTANT
     *
     * When we set restaurant:
     * → restaurant_id stored in DB
     */
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}