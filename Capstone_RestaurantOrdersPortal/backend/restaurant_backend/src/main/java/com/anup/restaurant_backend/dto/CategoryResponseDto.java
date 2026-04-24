package com.anup.restaurant_backend.dto;

public class CategoryResponseDto {
    private Long id;
    private String name;
    private Long restaurantId;

    public CategoryResponseDto() {}

    public CategoryResponseDto(Long id, String name, Long restaurantId) {
        this.id = id;
        this.name = name;
        this.restaurantId = restaurantId;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Long getRestaurantId() { return restaurantId; }
}