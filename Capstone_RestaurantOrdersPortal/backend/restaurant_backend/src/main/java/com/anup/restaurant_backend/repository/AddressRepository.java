package com.anup.restaurant_backend.repository;

import com.anup.restaurant_backend.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 *  AddressRepository
 *
 *  What this file does:
 * Handles DB operations for Address
 */
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     *  Get all addresses of a user
     */
    List<Address> findByUserId(Long userId);
}