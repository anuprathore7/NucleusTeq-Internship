package com.anup.restaurant_backend.controller;
import com.anup.restaurant_backend.dto.CategoryRequestDto;
import com.anup.restaurant_backend.dto.CategoryResponseDto;
import com.anup.restaurant_backend.service.CategoryService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * This controller handles category management for a restaurant.
 * It allows owners to create, update, and delete categories,
 * while customers can view available categories for browsing.
 */
@RestController
@RequestMapping(CategoryController.BASE_URL)
public class CategoryController {
    public static final String BASE_URL="/api/restaurants/{restaurantId}/categories";
    public static final String UPDATE="/{categoryId}";
    private final CategoryService categoryService;
    public CategoryController(CategoryService categoryService){this.categoryService=categoryService;}
    /**
     * Adds a new category to a specific restaurant.
     * Only the restaurant owner is allowed to perform this action.
     */
    @PostMapping
    public CategoryResponseDto addCategory(@PathVariable Long restaurantId,@RequestBody CategoryRequestDto request,@RequestHeader("Authorization") String token){
        return categoryService.addCategory(restaurantId,request,token);
    }
    /**
     * Returns all categories for a given restaurant.
     * This is a public API used by customers to explore menu sections.
     */
    @GetMapping
    public List<CategoryResponseDto> getCategoriesByRestaurant(@PathVariable Long restaurantId){
        return categoryService.getCategoriesByRestaurant(restaurantId);
    }
    /**
     * Updates the name or details of an existing category.
     * This action is restricted to the restaurant owner.
     */
    @PutMapping(UPDATE)
    public CategoryResponseDto updateCategory(@PathVariable Long restaurantId,@PathVariable Long categoryId,@RequestBody CategoryRequestDto request,@RequestHeader("Authorization") String token){
        return categoryService.updateCategory(categoryId,request,token);
    }
    /**
     * Deletes a category from the restaurant.
     * Only the owner can remove categories.
     */
    @DeleteMapping(UPDATE)
    public String deleteCategory(@PathVariable Long restaurantId,@PathVariable Long categoryId,@RequestHeader("Authorization") String token){
        categoryService.deleteCategory(categoryId,token);
        return "Category deleted successfully";
    }
}