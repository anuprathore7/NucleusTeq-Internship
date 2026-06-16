package com.anup.restaurant_backend.repository;

import com.anup.restaurant_backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 *  CategoryRepository
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     *  Get categories of a restaurant
     */
    List<Category> findByRestaurantId(Long restaurantId);
}