package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.*;
import com.anup.restaurant_backend.entity.Restaurant;
import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.enums.UserRole;
import com.anup.restaurant_backend.exception.ResourceNotFoundException;
import com.anup.restaurant_backend.repository.RestaurantRepository;
import com.anup.restaurant_backend.repository.UserRepository;
import com.anup.restaurant_backend.security.JwtService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private static final Logger log = LoggerFactory.getLogger(RestaurantServiceImpl.class);

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;


    public RestaurantServiceImpl(RestaurantRepository restaurantRepository,
                                 UserRepository userRepository,
                                 JwtService jwtService) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public RestaurantResponseDto createRestaurant(
            RestaurantRequestDto request,
            String token) {

        String jwt = token.substring(7);

        //  Extract email from token
        String email = jwtService.extractEmail(jwt);

        //  Fetch logged-in user
        UserEntity owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Role check
        if (owner.getRole() != UserRole.RESTAURANT_OWNER) {
            throw new RuntimeException("Only restaurant owners can create restaurant");
        }

        //  Create restaurant
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());
        restaurant.setOwner(owner);

        Restaurant saved = restaurantRepository.save(restaurant);

        return mapToResponse(saved);
    }

    @Override
    public List<RestaurantResponseDto> getAllRestaurants() {

        log.info("Fetching all restaurants");

        List<RestaurantResponseDto> list = restaurantRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.info("Total restaurants found: {}", list.size());

        return list;
    }

    @Override
    public RestaurantResponseDto getRestaurantById(Long id) {

        log.info("Fetching restaurant with id: {}", id);

        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Restaurant not found with id: {}", id);
                    return new ResourceNotFoundException("Restaurant not found");
                });

        return mapToResponse(r);
    }

    @Override
    public List<RestaurantResponseDto> getRestaurantsByOwner(Long ownerId) {

        log.info("Fetching restaurants for owner id: {}", ownerId);

        List<RestaurantResponseDto> list = restaurantRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        log.info("Total restaurants found for owner {}: {}", ownerId, list.size());

        return list;
    }

    /**
     * ENTITY → DTO
     */
    private RestaurantResponseDto mapToResponse(Restaurant r) {
        return new RestaurantResponseDto(
                r.getId(),
                r.getName(),
                r.getDescription(),
                r.getAddress(),
                r.getPhone(),
                r.getOwner().getId()
        );
    }
}