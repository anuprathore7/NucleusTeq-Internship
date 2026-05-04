package com.anup.restaurant_backend.repository;

import com.anup.restaurant_backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *  OrderItemRepository
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}