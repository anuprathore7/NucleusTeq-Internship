package com.anup.restaurant_backend.repository;

import com.anup.restaurant_backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 *  OrderRepository
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     *  Get all orders of user
     */
    List<Order> findByUserId(Long userId);
}