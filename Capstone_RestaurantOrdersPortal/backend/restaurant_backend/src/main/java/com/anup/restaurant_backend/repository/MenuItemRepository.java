package com.anup.restaurant_backend.repository;

import com.anup.restaurant_backend.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ============================================
 *   MenuItemRepository
 * ============================================
 *
 *  JpaRepository gives us free methods like:
 * → save(), findById(), deleteById(), existsById()
 *
 *  CUSTOM METHOD:
 * findByRestaurantId → Spring auto-generates this query:
 * "SELECT * FROM menu_items WHERE restaurant_id = ?"
 *
 * No SQL needed — Spring reads the method name and builds query itself!
 */
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    /**
     * Get all menu items for a specific restaurant
     * Used when customer opens a restaurant to see the menu
     */
    List<MenuItem> findByRestaurantId(Long restaurantId);
}