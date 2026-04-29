package com.anup.restaurant_backend.entity;

import jakarta.persistence.*;

/**
 * Address Entity
 * This class represents the "addresses" table in database.
 * It stores delivery locations of users.
 */
@Entity
@Table(name = "addresses")
public class Address {

    /**
     * Primary Key
     * This is unique ID for every address
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     *  Street details (House no, street name, area)
     */
    private String street;

    /**
     *  City name
     */
    private String city;

    /**
     *  State name
     */
    private String state;

    /**
     *  Pincode for delivery
     */
    private String pincode;

    /**
     *  Relationship with User
     *
     * MANY addresses → belong to ONE user
     *
     *  This will create a foreign key column "user_id" in addresses table
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /**
     *  Default Constructor (Required by JPA)
     *
     * JPA (Hibernate) uses this to create object internally
     */
    public Address() {
    }

    // ================= GETTERS & SETTERS =================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public UserEntity getUser() {
        return user;
    }

    /**
     * 🧠 IMPORTANT FLOW UNDERSTANDING
     *
     * When we set user here:
     * → This address gets linked to that user
     * → user_id column will be filled in DB
     */
    public void setUser(UserEntity user) {
        this.user = user;
    }
}