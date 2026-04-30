package com.anup.restaurant_backend.repository;

import com.anup.restaurant_backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for performing database operations on UserEntity.
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Finds a user by their email address. Used during login and JWT validation.
     */
    Optional<UserEntity> findByEmail(String email);
}