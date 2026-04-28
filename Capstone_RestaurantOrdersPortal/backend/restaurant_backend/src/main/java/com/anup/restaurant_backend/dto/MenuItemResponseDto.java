package com.anup.restaurant_backend.dto;

/**
 * ============================================
 *   MenuItemResponseDTO
 * ============================================
 *
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

    // ============= CONSTRUCTORS =============
    public MenuItemResponseDto() {}

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

    // ============= GETTERS =============
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Double getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public Boolean getAvailable() { return available; }
    public Long getCategoryId() { return categoryId; }
    public Long getRestaurantId() { return restaurantId; }
}