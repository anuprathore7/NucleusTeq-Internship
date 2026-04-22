package com.anup.restaurant_backend.repository;

import com.anup.restaurant_backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *  CartItemRepository
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}