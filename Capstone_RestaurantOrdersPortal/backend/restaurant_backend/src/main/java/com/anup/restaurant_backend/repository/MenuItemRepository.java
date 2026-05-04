package com.anup.restaurant_backend.repository;

import com.anup.restaurant_backend.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Menu Item Repository
 */
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    /**
     * Get all menu items for a specific restaurant
     * Used when customer opens a restaurant to see the menu
     */
    List<MenuItem> findByRestaurantId(Long restaurantId);
}