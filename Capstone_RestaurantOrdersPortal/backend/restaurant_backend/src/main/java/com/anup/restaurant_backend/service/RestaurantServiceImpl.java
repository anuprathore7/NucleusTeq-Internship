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

    /**
     * Creates a new restaurant for the authenticated owner.
     *
     * @param request restaurant details
     * @param token authentication token
     * @return created restaurant response
     */
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
    /**
     * Retrieves all restaurants available in the system.
     *
     * @return list of restaurant responses
     */
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
    /**
     * Fetches a restaurant by its ID.
     *
     * @param id restaurant ID
     * @return restaurant response
     */
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
    /**
     * Retrieves all restaurants owned by the authenticated user.
     *
     * @param token authentication token
     * @return list of restaurant responses
     */
    @Override
    public List<RestaurantResponseDto> getRestaurantsByOwnerToken(String token) {

        String jwt = token.substring(7); // remove Bearer

        String email = jwtService.extractEmail(jwt);

        UserEntity owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Restaurant> restaurants = restaurantRepository.findByOwnerId(owner.getId());

        return restaurants.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
                r.getOwner().getId(),
                r.getImagePath()
        );
    }
    /**
     * Retrieves all restaurants owned by the authenticated user.
     *
     * @param token authentication token
     * @return list of restaurant responses
     */
    @Override
    public RestaurantResponseDto updateRestaurant(Long id, RestaurantRequestDto request, String token) {
        String email = jwtService.extractEmail(token.substring(7));

        UserEntity owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        if (!restaurant.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You are not authorized to update this restaurant");
        }

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());

        Restaurant updated = restaurantRepository.save(restaurant);

        log.info("Owner '{}' updated restaurantId: {}", email, id);
        return mapToResponse(updated);
    }
    /**
     * Deletes a restaurant if the authenticated user is the owner.
     *
     * @param id restaurant ID
     * @param token authentication token
     */
    @Override
    public void deleteRestaurant(Long id, String token) {
        String email = jwtService.extractEmail(token.substring(7));

        UserEntity owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        if (!restaurant.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You are not authorized to delete this restaurant");
        }

        log.info("Owner '{}' deleting restaurantId: {}", email, id);
        restaurantRepository.deleteById(id);
        log.info("Restaurant deleted successfully");
    }
    /**
     * Updates the image of a restaurant.
     *
     * @param id restaurant ID
     * @param imagePath image path
     * @param token authentication token
     * @return updated restaurant response
     */
    @Override
    public RestaurantResponseDto updateRestaurantImage(Long id, String imagePath, String token) {
        String email = jwtService.extractEmail(token.substring(7));

        UserEntity owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        if (!restaurant.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You are not authorized to update this restaurant");
        }

        restaurant.setImagePath(imagePath);
        Restaurant updated = restaurantRepository.save(restaurant);

        log.info("Owner '{}' updated image for restaurantId: {}", email, id);
        return mapToResponse(updated);
    }


}