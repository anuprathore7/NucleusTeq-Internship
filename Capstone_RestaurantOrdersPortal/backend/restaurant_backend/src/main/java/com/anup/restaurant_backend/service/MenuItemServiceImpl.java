package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.MenuItemRequestDto;
import com.anup.restaurant_backend.dto.MenuItemResponseDto;
import com.anup.restaurant_backend.entity.Category;
import com.anup.restaurant_backend.entity.MenuItem;
import com.anup.restaurant_backend.entity.Restaurant;
import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.exception.ResourceNotFoundException;
import com.anup.restaurant_backend.repository.CategoryRepository;
import com.anup.restaurant_backend.repository.MenuItemRepository;
import com.anup.restaurant_backend.repository.RestaurantRepository;
import com.anup.restaurant_backend.repository.UserRepository;
import com.anup.restaurant_backend.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 *   MenuItemServiceImpl
 */
@Service
public class MenuItemServiceImpl implements MenuItemService {

    private static final Logger log = LoggerFactory.getLogger(MenuItemServiceImpl.class);

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;


    public MenuItemServiceImpl(MenuItemRepository menuItemRepository,
                               RestaurantRepository restaurantRepository,
                               CategoryRepository categoryRepository,
                               UserRepository userRepository,
                               JwtService jwtService) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public MenuItemResponseDto addMenuItem(Long restaurantId, MenuItemRequestDto request, String token) {


        String email = jwtService.extractEmail(token.substring(7));
        UserEntity owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));


        if (!restaurant.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You are not authorized to add items to this restaurant");
        }


        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));


        if (!category.getRestaurant().getId().equals(restaurantId)) {
            throw new RuntimeException("This category does not belong to restaurant id: " + restaurantId);
        }


        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setAvailable(request.getAvailable() != null ? request.getAvailable() : true);
        menuItem.setCategory(category);
        menuItem.setRestaurant(restaurant);

        MenuItem saved = menuItemRepository.save(menuItem);

        log.info("MenuItem '{}' added to restaurantId: {} by owner: {}", saved.getName(), restaurantId, email);

        return mapToResponse(saved);
    }


    /**
     *  Customer opens a restaurant page
     */
    @Override
    public List<MenuItemResponseDto> getMenuItemsByRestaurant(Long restaurantId) {

        log.info("Fetching menu items for restaurantId: {}", restaurantId);

        List<MenuItem> items = menuItemRepository.findByRestaurantId(restaurantId);

        log.info("Found {} items for restaurantId: {}", items.size(), restaurantId);

        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     *  Owner wants to change price or availability of an item
     */
    @Override
    public MenuItemResponseDto updateMenuItem(Long menuItemId, MenuItemRequestDto request, String token) {

        // Step 1: Get owner from JWT
        String email = jwtService.extractEmail(token.substring(7));
        UserEntity owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 2: Find the menu item
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem not found with id: " + menuItemId));

        // Step 3: Ownership check via menuItem → restaurant → owner
        if (!menuItem.getRestaurant().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You are not authorized to update this menu item");
        }

        // Step 4: Update only provided fields
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setAvailable(request.getAvailable());

        // If category is being changed
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            menuItem.setCategory(category);
        }

        MenuItem updated = menuItemRepository.save(menuItem);

        log.info("MenuItem '{}' updated by owner: {}", updated.getName(), email);

        return mapToResponse(updated);
    }


    /**
     *  Owner removes an item from menu
     */
    @Override
    public void deleteMenuItem(Long menuItemId, String token) {


        String email = jwtService.extractEmail(token.substring(7));
        UserEntity owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));


        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem not found with id: " + menuItemId));


        if (!menuItem.getRestaurant().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You are not authorized to delete this menu item");
        }


        menuItemRepository.deleteById(menuItemId);

        log.info("MenuItem id: {} deleted by owner: {}", menuItemId, email);
    }



    /**
     *  Converts MenuItem entity → MenuItemResponseDTO
     * We extract only ids from nested objects (category, restaurant)
     * to keep response flat and clean
     */
    private MenuItemResponseDto mapToResponse(MenuItem item) {
        return new MenuItemResponseDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getImageUrl(),
                item.getAvailable(),
                item.getCategory().getId(),
                item.getRestaurant().getId()
        );
    }
}