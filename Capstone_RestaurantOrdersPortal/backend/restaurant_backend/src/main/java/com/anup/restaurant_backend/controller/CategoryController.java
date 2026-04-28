package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.CategoryRequestDto;
import com.anup.restaurant_backend.dto.CategoryResponseDto;
import com.anup.restaurant_backend.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CategoryController
 *
 *
 */
@RestController
@RequestMapping("/api/restaurants/{restaurantId}/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * ADD CATEGORY
     * POST /api/restaurants/5/categories
     * Header: Authorization: Bearer <token>
     * Body: { "name": "Starters" }
     */
    @PostMapping
    public CategoryResponseDto addCategory(
            @PathVariable Long restaurantId,
            @RequestBody CategoryRequestDto request,
            @RequestHeader("Authorization") String token) {

        return categoryService.addCategory(restaurantId, request, token);
    }

    /**
     * GET ALL CATEGORIES OF A RESTAURANT
     * GET /api/restaurants/5/categories
     * No token needed - public
     */
    @GetMapping
    public List<CategoryResponseDto> getCategoriesByRestaurant(
            @PathVariable Long restaurantId) {

        return categoryService.getCategoriesByRestaurant(restaurantId);
    }

    /**
     * UPDATE CATEGORY
     * PUT /api/restaurants/5/categories/3
     * Header: Authorization: Bearer <token>
     * Body: { "name": "Appetizers" }
     */
    @PutMapping("/{categoryId}")
    public CategoryResponseDto updateCategory(
            @PathVariable Long restaurantId,
            @PathVariable Long categoryId,
            @RequestBody CategoryRequestDto request,
            @RequestHeader("Authorization") String token) {

        return categoryService.updateCategory(categoryId, request, token);
    }

    /**
     * DELETE CATEGORY
     * DELETE /api/restaurants/5/categories/3
     * Header: Authorization: Bearer <token>
     */
    @DeleteMapping("/{categoryId}")
    public String deleteCategory(
            @PathVariable Long restaurantId,
            @PathVariable Long categoryId,
            @RequestHeader("Authorization") String token) {

        categoryService.deleteCategory(categoryId, token);
        return "Category deleted successfully";
    }
}