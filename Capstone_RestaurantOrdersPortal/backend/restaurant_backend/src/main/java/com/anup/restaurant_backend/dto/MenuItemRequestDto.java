package com.anup.restaurant_backend.dto;

/**
 * ============================================
 *   MenuItemRequestDTO
 * ============================================
 */
public class MenuItemRequestDto {

    /**
     * Name of the food item
     * Example: "Veg Pizza", "Cold Coffee", "Chicken Burger"
     */
    private String name;

    /**
     * Short description of the item
     * Example: "Crispy base with fresh veggies"
     */
    private String description;

    /**
     * Price of the item in rupees
     * Example: 199.0, 349.0
     */
    private Double price;

    /**
     * Image URL for the item (optional, used in frontend)
     * Example: "https://example.com/pizza.jpg"
     */
    private String imageUrl;

    /**
     * Is this item currently available?
     * true  = customer can order it
     * false = out of stock / hidden
     */
    private Boolean available;

    /**
     * Which category does this item belong to?
     * Example: categoryId=3 means "Starters"
     *
     * Category must belong to the same restaurant.
     * We verify this in service layer.
     */
    private Long categoryId;

    // ============= CONSTRUCTORS =============
    public MenuItemRequestDto() {}

    // ============= GETTERS & SETTERS =============
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}