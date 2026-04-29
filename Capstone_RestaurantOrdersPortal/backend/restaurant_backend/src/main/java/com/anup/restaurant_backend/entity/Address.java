package com.anup.restaurant_backend.entity;

import jakarta.persistence.*;

/**
 * Entity representing a user's saved delivery address stored in the "addresses" table.
 */
@Entity
@Table(name = "addresses")
public class Address {

    /** Primary key, auto-incremented by the database. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Street details such as house number, street name, and area. */
    private String street;

    /** City name for this address. */
    private String city;

    /** State name for this address. */
    private String state;

    /** Postal pincode used for delivery routing. */
    private String pincode;

    /**
     * The user who owns this address.
     * Many addresses can belong to one user.
     * Creates a foreign key column "user_id" in the addresses table.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    /** Default constructor required by JPA. */
    public Address() {}

    public Long getId()              { return id; }
    public void setId(Long id)       { this.id = id; }

    public String getStreet()        { return street; }
    public void setStreet(String s)  { this.street = s; }

    public String getCity()          { return city; }
    public void setCity(String c)    { this.city = c; }

    public String getState()         { return state; }
    public void setState(String s)   { this.state = s; }

    public String getPincode()       { return pincode; }
    public void setPincode(String p) { this.pincode = p; }

    public UserEntity getUser()           { return user; }

    /**
     * Links this address to the given user, setting the user_id foreign key in the database.
     */
    public void setUser(UserEntity user)  { this.user = user; }
}