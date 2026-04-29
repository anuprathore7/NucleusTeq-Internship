package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.AddressRequestDto;
import com.anup.restaurant_backend.dto.AddressResponseDto;
import com.anup.restaurant_backend.service.AddressService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing user delivery addresses.
 * All endpoints require a valid JWT token.
 */
@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    /**
     * Returns all addresses saved by the authenticated user.
     * GET /api/addresses
     */
    @GetMapping
    public List<AddressResponseDto> getMyAddresses(
            @RequestHeader("Authorization") String token) {
        return addressService.getMyAddresses(token);
    }

    /**
     * Saves a new delivery address for the authenticated user.
     * POST /api/addresses
     */
    @PostMapping
    public AddressResponseDto addAddress(
            @RequestBody AddressRequestDto request,
            @RequestHeader("Authorization") String token) {
        return addressService.addAddress(request, token);
    }

    /**
     * Deletes an address by ID if it belongs to the authenticated user.
     * DELETE /api/addresses/{id}
     */
    @DeleteMapping("/{id}")
    public String deleteAddress(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        addressService.deleteAddress(id, token);
        return "Address deleted successfully";
    }
}