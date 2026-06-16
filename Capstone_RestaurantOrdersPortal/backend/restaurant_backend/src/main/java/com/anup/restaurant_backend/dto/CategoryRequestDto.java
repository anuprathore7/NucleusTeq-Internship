package com.anup.restaurant_backend.dto;

/**
 * Request object used when creating or updating a category.
 * It mainly carries the category name from client to server.
 */
public class CategoryRequestDto {

    private String name;

    /**
     * Default constructor.
     */
    public CategoryRequestDto() {}

    /**
     * Returns category name.
     */
    public String getName() { return name; }

    /**
     * Sets category name.
     */
    public void setName(String name) { this.name = name; }
}