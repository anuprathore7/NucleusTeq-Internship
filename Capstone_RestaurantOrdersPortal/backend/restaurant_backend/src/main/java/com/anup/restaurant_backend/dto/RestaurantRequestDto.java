package com.anup.restaurant_backend.dto;

/**
 * Request object used to create or update a restaurant.
 * Contains basic details like name, address, and contact info.
 */
public class RestaurantRequestDto {

    private String name;
    private String description;
    private String address;
    private String phone;

    /**
     * Returns restaurant name.
     */
    public String getName() { return name; }

    /**
     * Sets restaurant name.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns description.
     */
    public String getDescription() { return description; }

    /**
     * Sets description.
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Returns address.
     */
    public String getAddress() { return address; }

    /**
     * Sets address.
     */
    public void setAddress(String address) { this.address = address; }

    /**
     * Returns phone number.
     */
    public String getPhone() { return phone; }

    /**
     * Sets phone number.
     */
    public void setPhone(String phone) { this.phone = phone; }
}