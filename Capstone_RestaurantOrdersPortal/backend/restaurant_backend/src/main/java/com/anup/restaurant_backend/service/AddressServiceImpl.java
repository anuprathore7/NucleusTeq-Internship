package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.AddressRequestDto;
import com.anup.restaurant_backend.dto.AddressResponseDto;
import com.anup.restaurant_backend.entity.Address;
import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.repository.AddressRepository;
import com.anup.restaurant_backend.repository.UserRepository;
import com.anup.restaurant_backend.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of AddressService for managing user delivery addresses.
 */
@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository    userRepository;
    private final JwtService        jwtService;

    public AddressServiceImpl(AddressRepository addressRepository,
                              UserRepository userRepository,
                              JwtService jwtService) {
        this.addressRepository = addressRepository;
        this.userRepository    = userRepository;
        this.jwtService        = jwtService;
    }

    /**
     * Returns all addresses saved by the authenticated user.
     */
    @Override
    public List<AddressResponseDto> getMyAddresses(String token) {
        UserEntity user = getUserFromToken(token);
        return addressRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates and saves a new address linked to the authenticated user.
     */
    @Override
    @Transactional
    public AddressResponseDto addAddress(AddressRequestDto request, String token) {
        UserEntity user = getUserFromToken(token);

        Address address = new Address();
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setUser(user);

        Address saved = addressRepository.save(address);
        return mapToResponse(saved);
    }

    /**
     * Deletes an address after verifying it belongs to the authenticated user.
     */
    @Override
    @Transactional
    public void deleteAddress(Long addressId, String token) {
        UserEntity user = getUserFromToken(token);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + addressId));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to delete this address.");
        }

        addressRepository.deleteById(addressId);
    }

    /**
     * Extracts the authenticated user from the Bearer token.
     */
    private UserEntity getUserFromToken(String token) {
        String email = jwtService.extractEmail(token.substring(7));
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Converts an Address entity to a response DTO.
     */
    private AddressResponseDto mapToResponse(Address address) {
        return new AddressResponseDto(
                address.getId(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getPincode()
        );
    }
}