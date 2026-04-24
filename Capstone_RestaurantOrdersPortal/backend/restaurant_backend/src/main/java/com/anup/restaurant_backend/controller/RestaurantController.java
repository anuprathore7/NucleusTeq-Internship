package com.anup.restaurant_backend.controller;


import com.anup.restaurant_backend.dto.RestaurantRequestDto;
import com.anup.restaurant_backend.dto.RestaurantResponseDto;
import com.anup.restaurant_backend.service.RestaurantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService){
        this.restaurantService = restaurantService;


    }

    @PostMapping
    public RestaurantResponseDto createRestaurant(
            @RequestBody RestaurantRequestDto request,
            @RequestHeader("Authorization") String token) {

        return restaurantService.createRestaurant(request, token);
    }

    @GetMapping
    public List<RestaurantResponseDto> getAll() {
        return restaurantService.getAllRestaurants();
    }

    @GetMapping("/{id}")
    public RestaurantResponseDto getById(@PathVariable Long id) {
        return restaurantService.getRestaurantById(id);
    }

    @GetMapping("/owner/{ownerId}")
    public List<RestaurantResponseDto> getByOwner(@PathVariable Long ownerId) {
        return restaurantService.getRestaurantsByOwner(ownerId);
    }

    @PutMapping("/{id}")
    public RestaurantResponseDto updateRestaurant(
            @PathVariable Long id,
            @RequestBody RestaurantRequestDto request,
            @RequestHeader("Authorization") String token) {
        return restaurantService.updateRestaurant(id, request, token);
    }

    @DeleteMapping("/{id}")
    public String deleteRestaurant(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        restaurantService.deleteRestaurant(id, token);
        return "Restaurant deleted successfully";
    }


}
