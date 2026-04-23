package com.anup.restaurant_backend.service;
import com.anup.restaurant_backend.dto.*;
import com.anup.restaurant_backend.dto.RestaurantResponseDto;


import java.util.List;

public interface RestaurantService {

    RestaurantResponseDto createRestaurant(
            RestaurantRequestDto request,
            String token
    );

    List<RestaurantResponseDto> getAllRestaurants();

    RestaurantResponseDto getRestaurantById(Long id);

    List<RestaurantResponseDto> getRestaurantsByOwner(Long ownerId);
}