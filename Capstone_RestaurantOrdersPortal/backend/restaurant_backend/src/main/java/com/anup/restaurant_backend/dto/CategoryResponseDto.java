package com.anup.restaurant_backend.dto;

/**
 * Response object for category data.
 * It contains basic information about category and its restaurant.
 */
public class CategoryResponseDto {

    private Long id;
    private String name;
    private Long restaurantId;

    /**
     * Default constructor.
     */
    public CategoryResponseDto() {}

    /**
     * Creates category response with all fields.
     */
    public CategoryResponseDto(Long id, String name, Long restaurantId) {
        this.id = id;
        this.name = name;
        this.restaurantId = restaurantId;
    }

    /**
     * Returns category ID.
     */
    public Long getId() { return id; }

    /**
     * Returns category name.
     */
    public String getName() { return name; }

    /**
     * Returns associated restaurant ID.
     */
    public Long getRestaurantId() { return restaurantId; }
}