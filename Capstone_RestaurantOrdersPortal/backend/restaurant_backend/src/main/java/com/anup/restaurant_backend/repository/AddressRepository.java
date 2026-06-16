package com.anup.restaurant_backend.repository;

import com.anup.restaurant_backend.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for performing database operations on Address entities.
 */
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Fetches all addresses belonging to a specific user.
     */
    List<Address> findByUserId(Long userId);
}