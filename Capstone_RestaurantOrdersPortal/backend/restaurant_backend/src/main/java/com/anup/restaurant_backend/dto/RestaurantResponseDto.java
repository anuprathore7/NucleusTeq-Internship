package com.anup.restaurant_backend.dto;

public class RestaurantResponseDto {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private Long ownerId;
    private String imagePath;

    public RestaurantResponseDto() {}

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

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public Long getOwnerId() { return ownerId; }
    public String getImagePath() { return imagePath; }
}