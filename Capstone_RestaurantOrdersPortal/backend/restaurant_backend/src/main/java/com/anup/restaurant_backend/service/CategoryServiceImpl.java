package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.CategoryRequestDto;
import com.anup.restaurant_backend.dto.CategoryResponseDto;
import com.anup.restaurant_backend.entity.Category;
import com.anup.restaurant_backend.entity.Restaurant;
import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.exception.ResourceNotFoundException;
import com.anup.restaurant_backend.repository.CategoryRepository;
import com.anup.restaurant_backend.repository.RestaurantRepository;
import com.anup.restaurant_backend.repository.UserRepository;
import com.anup.restaurant_backend.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation responsible for managing category-related operations
 * such as adding, updating, retrieving, and deleting categories.
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    // Constructor injection - same style as your RestaurantServiceImpl
    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               RestaurantRepository restaurantRepository,
                               UserRepository userRepository,
                               JwtService jwtService) {
        this.categoryRepository = categoryRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /**
     * Adds a new category to a restaurant owned by the authenticated user.
     *
     * @param restaurantId restaurant ID
     * @param request category data
     * @param token authentication token
     * @return created category response
     */
    @Override
    public CategoryResponseDto addCategory(Long restaurantId, CategoryRequestDto request, String token) {

        String email = jwtService.extractEmail(token.substring(7));

        UserEntity owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find restaurant
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        // OWNERSHIP CHECK - owner can only add to their own restaurant
        if (!restaurant.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You are not authorized to add categories to this restaurant");
        }

        log.info("Owner '{}' adding category '{}' to restaurantId: {}", email, request.getName(), restaurantId);

        Category category = new Category();
        category.setName(request.getName());
        category.setRestaurant(restaurant);

        Category saved = categoryRepository.save(category);

        log.info("Category saved with id: {}", saved.getId());

        return mapToResponse(saved);
    }
    /**
     * Retrieves all categories for a given restaurant.
     *
     * @param restaurantId restaurant ID
     * @return list of categories
     */
    @Override
    public List<CategoryResponseDto> getCategoriesByRestaurant(Long restaurantId) {

        log.info("Fetching categories for restaurantId: {}", restaurantId);

        List<Category> categories = categoryRepository.findByRestaurantId(restaurantId);

        log.info("Found {} categories for restaurantId: {}", categories.size(), restaurantId);

        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates a category if the authenticated user is the owner.
     *
     * @param categoryId category ID
     * @param request updated data
     * @param token authentication token
     * @return updated category response
     */
    @Override
    public CategoryResponseDto updateCategory(Long categoryId, CategoryRequestDto request, String token) {

        String email = jwtService.extractEmail(token.substring(7));

        UserEntity owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        // Ownership check via category → restaurant → owner
        if (!category.getRestaurant().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You are not authorized to update this category");
        }

        log.info("Updating categoryId: {} to name: {}", categoryId, request.getName());

        category.setName(request.getName());
        Category updated = categoryRepository.save(category);

        return mapToResponse(updated);
    }

    /**
     * Deletes a category if the authenticated user is the owner.
     *
     * @param categoryId category ID
     * @param token authentication token
     */
    @Override
    public void deleteCategory(Long categoryId, String token) {

        String email = jwtService.extractEmail(token.substring(7));

        UserEntity owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        // Ownership check
        if (!category.getRestaurant().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("You are not authorized to delete this category");
        }

        log.info("Deleting categoryId: {} by owner: {}", categoryId, email);

        categoryRepository.deleteById(categoryId);

        log.info("Category deleted successfully");
    }


    private CategoryResponseDto mapToResponse(Category category) {
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getRestaurant().getId()
        );
    }
}