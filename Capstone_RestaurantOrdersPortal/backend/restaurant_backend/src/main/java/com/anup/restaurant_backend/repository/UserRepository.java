package com.anup.restaurant_backend.repository;

import com.anup.restaurant_backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // It will run a query in the database findbyEmail so that we can use this method in our service file.
    Optional<UserEntity> findByEmail(String email);
}