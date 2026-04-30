package com.anup.restaurant_backend.dto;

/**
 * Request object for creating or updating a menu item.
 * It carries all details required to define a food item.
 */
public class MenuItemRequestDto {

    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private Boolean available;
    private Long categoryId;

    /**
     * Default constructor.
     */
    public MenuItemRequestDto() {}

    /**
     * Returns item name.
     */
    public String getName() { return name; }

    /**
     * Sets item name.
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns description of the item.
     */
    public String getDescription() { return description; }

    /**
     * Sets description.
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Returns price.
     */
    public Double getPrice() { return price; }

    /**
     * Sets price.
     */
    public void setPrice(Double price) { this.price = price; }

    /**
     * Returns image URL.
     */
    public String getImageUrl() { return imageUrl; }

    /**
     * Sets image URL.
     */
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /**
     * Returns availability status.
     */
    public Boolean getAvailable() { return available; }

    /**
     * Sets availability.
     */
    public void setAvailable(Boolean available) { this.available = available; }

    /**
     * Returns category ID.
     */
    public Long getCategoryId() { return categoryId; }

    /**
     * Sets category ID.
     */
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}