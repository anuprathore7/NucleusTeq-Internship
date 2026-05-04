package com.anup.restaurant_backend.entity;

import jakarta.persistence.*;

/**
 *   Category Entity
 *
 * This class represents food categories inside a restaurant
 *
 *   Real-life meaning:
 * A restaurant has multiple categories like:
 * → Starters
 * → Main Course
 * → Drinks
 * → Desserts
 */
@Entity
@Table(name = "categories")
public class Category {

    /**
     *   Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     *   Category Name
     * Example: Pizza, Drinks, Dessert
     */
    private String name;

    /**
     * 🔗 RELATIONSHIP WITH RESTAURANT
     *
     * MANY categories → belong to ONE restaurant
     */
    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    /**
     *  Default constructor (required by JPA)
     */
    public Category() {
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
     *   Flow:
     * When we set name → stored in DB
     */
    public void setName(String name) {
        this.name = name;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    /**
     *   MOST IMPORTANT
     *
     * When we set restaurant:
     * → restaurant_id gets stored in DB
     * → this category is linked to that restaurant
     */
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}