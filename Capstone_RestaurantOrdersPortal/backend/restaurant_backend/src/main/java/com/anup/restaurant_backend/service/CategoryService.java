package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.CategoryRequestDto;
import com.anup.restaurant_backend.dto.CategoryResponseDto;

import java.util.List;

public interface CategoryService {
    CategoryResponseDto addCategory(Long restaurantId, CategoryRequestDto request, String token);
    List<CategoryResponseDto> getCategoriesByRestaurant(Long restaurantId);
    CategoryResponseDto updateCategory(Long categoryId, CategoryRequestDto request, String token);
    void deleteCategory(Long categoryId, String token);
}