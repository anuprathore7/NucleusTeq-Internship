package com.anup.restaurant_backend.dto;

/**
 * Response object representing restaurant details.
 * Includes owner info and image path for frontend usage.
 */
public class RestaurantResponseDto {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private Long ownerId;
    private String imagePath;

    /**
     * Default constructor.
     */
    public RestaurantResponseDto() {}

    /**
     * Creates restaurant response with all details.
     */
    public RestaurantResponseDto(Long id, String name, String description,
                                 String address, String phone, Long ownerId , String imagePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.ownerId = ownerId;
        this.imagePath = imagePath;
    }

    /**
     * Returns restaurant ID.
     */
    public Long getId() { return id; }

    /**
     * Returns name.
     */
    public String getName() { return name; }

    /**
     * Returns description.
     */
    public String getDescription() { return description; }

    /**
     * Returns address.
     */
    public String getAddress() { return address; }

    /**
     * Returns phone.
     */
    public String getPhone() { return phone; }

    /**
     * Returns owner ID.
     */
    public Long getOwnerId() { return ownerId; }

    /**
     * Returns image path.
     */
    public String getImagePath() { return imagePath; }
}