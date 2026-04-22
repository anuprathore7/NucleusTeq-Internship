package com.anup.restaurant_backend.entity;

import jakarta.persistence.*;

import java.util.List;

/**
 *  Restaurant Entity
 *
 * This class represents restaurants in the system.
 *
 *  Real-life meaning:
 * A restaurant is owned by a user (RESTAURANT_OWNER)
 * and contains menu items, categories, etc.
 */
@Entity
@Table(name = "restaurants")
public class Restaurant {

    /**
     *  Primary Key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     *  Restaurant Name
     */
    private String name;

    /**
     *  Description (about restaurant)
     */
    private String description;

    /**
     *  Location / Address (simple text for now)
     * (We can later link it with Address entity if needed)
     */
    private String address;

    /**
     *  Contact number of restaurant
     */
    private String phone;

    /**
     *  OWNER RELATIONSHIP
     *
     * ONE restaurant → belongs to ONE owner (User)
     * ONE owner → can have MANY restaurants
     */
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private UserEntity owner;

    @OneToMany(mappedBy = "restaurant")
    private List<Category> categories;

    /**
     *  Default constructor (required by JPA)
     */
    public Restaurant() {
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
     * When name is set → it will be stored in DB column
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

    public String getAddress() {
        return address;
    }

    /**
     * ⚠️ This is NOT Address entity
     * This is just simple string (quick solution)
     */
    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserEntity getOwner() {
        return owner;
    }

    /**
     *  MOST IMPORTANT LINE
     *
     * This sets:
     * → which user owns this restaurant
     * → fills owner_id in DB
     */
    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }
}