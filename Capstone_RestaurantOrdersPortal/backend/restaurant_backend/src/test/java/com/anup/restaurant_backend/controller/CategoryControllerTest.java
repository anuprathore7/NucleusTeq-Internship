package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.CategoryRequestDto;
import com.anup.restaurant_backend.dto.CategoryResponseDto;
import com.anup.restaurant_backend.security.CustomUserDetailsService;
import com.anup.restaurant_backend.service.CategoryService;
import com.anup.restaurant_backend.security.JwtService; // ✅ REQUIRED FIX
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test class for {@link CategoryController}.
 */
@WebMvcTest(controllers = CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    /** MockMvc is used to simulate HTTP requests */
    @Autowired
    private MockMvc mockMvc;

    /** ObjectMapper converts Java objects to JSON */
    @Autowired
    private ObjectMapper objectMapper;

    /** Mocked service layer dependency */
    @MockBean
    private CategoryService categoryService;

    /**
     * FIX: Mock JwtService to prevent ApplicationContext failure.
     * Even though filters are disabled, Spring still tries to create beans.
     */
    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private static final String TOKEN   = "Bearer mocked.jwt.token";
    private static final Long REST_ID   = 5L;
    private static final Long CAT_ID    = 3L;
    private static final String BASE    = "/api/restaurants/" + REST_ID + "/categories";

    private CategoryRequestDto requestDto;
    private CategoryResponseDto responseDto;

    /**
     * Initializes reusable test data before each test.
     */
    @BeforeEach
    void setUp() {
        requestDto = new CategoryRequestDto();
        requestDto.setName("Starters");

        responseDto = new CategoryResponseDto(CAT_ID, "Starters", REST_ID);
    }

    /**
     * Tests successful category creation with valid request and token.
     */
    @Test
    @DisplayName("addCategory — 200 when request is valid")
    void addCategory_validRequest_returns200() throws Exception {
        when(categoryService.addCategory(eq(REST_ID), any(CategoryRequestDto.class), eq(TOKEN)))
                .thenReturn(responseDto);

        mockMvc.perform(post(BASE)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CAT_ID))
                .andExpect(jsonPath("$.name").value("Starters"))
                .andExpect(jsonPath("$.restaurantId").value(REST_ID));

        verify(categoryService, times(1))
                .addCategory(eq(REST_ID), any(CategoryRequestDto.class), eq(TOKEN));
    }

    /**
     * Tests that missing token results in 400 Bad Request.
     */
    @Test
    @DisplayName("addCategory — 400 when token is missing")
    void addCategory_missingToken_returns400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests that missing request body results in 400 Bad Request.
     */
    @Test
    @DisplayName("addCategory — 400 when body is missing")
    void addCategory_missingBody_returns400() throws Exception {
        mockMvc.perform(post(BASE)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests fetching categories for a restaurant.
     */
    @Test
    @DisplayName("getCategoriesByRestaurant — 200 with category list")
    void getCategoriesByRestaurant_returns200_withList() throws Exception {
        when(categoryService.getCategoriesByRestaurant(REST_ID))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(CAT_ID))
                .andExpect(jsonPath("$[0].name").value("Starters"));

        verify(categoryService, times(1)).getCategoriesByRestaurant(REST_ID);
    }

    /**
     * Tests fetching categories when none exist.
     */
    @Test
    @DisplayName("getCategoriesByRestaurant — 200 with empty list when none exist")
    void getCategoriesByRestaurant_returnsEmptyList_whenNoneExist() throws Exception {
        when(categoryService.getCategoriesByRestaurant(REST_ID)).thenReturn(List.of());

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    /**
     * Tests updating a category successfully.
     */
    @Test
    @DisplayName("updateCategory — 200 when request is valid")
    void updateCategory_validRequest_returns200() throws Exception {
        CategoryResponseDto updated = new CategoryResponseDto(CAT_ID, "Appetizers", REST_ID);

        when(categoryService.updateCategory(eq(CAT_ID), any(CategoryRequestDto.class), eq(TOKEN)))
                .thenReturn(updated);

        mockMvc.perform(put(BASE + "/" + CAT_ID)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Appetizers"));

        verify(categoryService, times(1))
                .updateCategory(eq(CAT_ID), any(CategoryRequestDto.class), eq(TOKEN));
    }

    /**
     * Tests update failure when token is missing.
     */
    @Test
    @DisplayName("updateCategory — 400 when token is missing")
    void updateCategory_missingToken_returns400() throws Exception {
        mockMvc.perform(put(BASE + "/" + CAT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }



    /**
     * Tests successful deletion of a category.
     */
    @Test
    @DisplayName("deleteCategory — 200 with success message")
    void deleteCategory_validRequest_returns200() throws Exception {
        doNothing().when(categoryService).deleteCategory(CAT_ID, TOKEN);

        mockMvc.perform(delete(BASE + "/" + CAT_ID)
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("Category deleted successfully"));

        verify(categoryService, times(1)).deleteCategory(CAT_ID, TOKEN);
    }

    /**
     * Tests deletion failure when token is missing.
     */
    @Test
    @DisplayName("deleteCategory — 400 when token is missing")
    void deleteCategory_missingToken_returns400() throws Exception {
        mockMvc.perform(delete(BASE + "/" + CAT_ID))
                .andExpect(status().isBadRequest());
    }
}