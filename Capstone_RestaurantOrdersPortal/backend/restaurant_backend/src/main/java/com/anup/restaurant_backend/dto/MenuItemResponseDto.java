package com.anup.restaurant_backend.dto;

/**
 * Response object representing a menu item.
 * It includes all details needed by frontend to display the item.
 */
public class MenuItemResponseDto {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private Boolean available;
    private Long categoryId;
    private Long restaurantId;

    /**
     * Default constructor.
     */
    public MenuItemResponseDto() {}

    /**
     * Creates a menu item response with all details.
     */
    public MenuItemResponseDto(Long id, String name, String description,
                               Double price, String imageUrl, Boolean available,
                               Long categoryId, Long restaurantId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.available = available;
        this.categoryId = categoryId;
        this.restaurantId = restaurantId;
    }

    /**
     * Returns item ID.
     */
    public Long getId() { return id; }

    /**
     * Returns item name.
     */
    public String getName() { return name; }

    /**
     * Returns description.
     */
    public String getDescription() { return description; }

    /**
     * Returns price.
     */
    public Double getPrice() { return price; }

    /**
     * Returns image URL.
     */
    public String getImageUrl() { return imageUrl; }

    /**
     * Returns availability.
     */
    public Boolean getAvailable() { return available; }

    /**
     * Returns category ID.
     */
    public Long getCategoryId() { return categoryId; }

    /**
     * Returns restaurant ID.
     */
    public Long getRestaurantId() { return restaurantId; }
}