package com.anup.restaurant_backend.repository;

import com.anup.restaurant_backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * OrderRepository
 *
 * findByUserId       → customer's order history
 * findByRestaurantId → owner sees incoming orders
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    // GET /api/orders/my-orders → customer sees their orders
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // GET /api/orders/restaurant/{id} → owner sees orders for their restaurant
    List<Order> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
}