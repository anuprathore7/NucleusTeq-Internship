package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.AddressRequestDto;
import com.anup.restaurant_backend.dto.AddressResponseDto;

import java.util.List;

/**
 * Service interface for managing user delivery addresses.
 */
public interface AddressService {

    /**
     * Returns all saved addresses for the authenticated user.
     */
    List<AddressResponseDto> getMyAddresses(String token);

    /**
     * Saves a new address for the authenticated user.
     */
    AddressResponseDto addAddress(AddressRequestDto request, String token);

    /**
     * Deletes an address by ID if it belongs to the authenticated user.
     */
    void deleteAddress(Long addressId, String token);
}