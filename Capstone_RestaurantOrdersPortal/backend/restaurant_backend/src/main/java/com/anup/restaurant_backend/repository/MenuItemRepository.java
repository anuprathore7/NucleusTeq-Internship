package com.anup.restaurant_backend.repository;

import com.anup.restaurant_backend.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 *  MenuItemRepository
 */
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    /**
     *  Get items by restaurant
     */
    List<MenuItem> findByRestaurantId(Long restaurantId);

    /**
     *  Get items by category
     */
    List<MenuItem> findByCategoryId(Long categoryId);
}