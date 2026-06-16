package com.anup.restaurant_backend.repository;

import com.anup.restaurant_backend.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 *  CartRepository
 */
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     *  One user → one cart
     */
    Optional<Cart> findByUserId(Long userId);
}