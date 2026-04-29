package com.anup.restaurant_backend.repository;

import com.anup.restaurant_backend.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 *  RestaurantRepository
 */
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    /**
     *  Find restaurants by owner
     */
    List<Restaurant> findByOwnerId(Long ownerId);
}