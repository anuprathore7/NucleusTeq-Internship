

package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.RestaurantRequestDto;
import com.anup.restaurant_backend.dto.RestaurantResponseDto;
import com.anup.restaurant_backend.service.RestaurantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    // Folder on disk where images will be stored
    // This is relative to wherever you run the Spring Boot app (project root)
    private static final String IMAGE_DIR = "images/";

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    // ─────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────
    @PostMapping
    public RestaurantResponseDto createRestaurant(
            @RequestBody RestaurantRequestDto request,
            @RequestHeader("Authorization") String token) {

        return restaurantService.createRestaurant(request, token);
    }

    // ─────────────────────────────────────────
    //  GET ALL (customer-facing)
    // ─────────────────────────────────────────
    @GetMapping
    public List<RestaurantResponseDto> getAll() {
        return restaurantService.getAllRestaurants();
    }

    // ─────────────────────────────────────────
    //  GET MY RESTAURANTS (owner dashboard)
    //  Uses token to figure out who the owner is
    // ─────────────────────────────────────────
    @GetMapping("/owner")
    public List<RestaurantResponseDto> getMyRestaurants(
            @RequestHeader("Authorization") String token) {

        return restaurantService.getRestaurantsByOwnerToken(token);
    }

    // ─────────────────────────────────────────
    //  GET SINGLE
    // ─────────────────────────────────────────
    @GetMapping("/{id}")
    public RestaurantResponseDto getById(@PathVariable Long id) {
        return restaurantService.getRestaurantById(id);
    }

    // ─────────────────────────────────────────
    //  UPDATE DETAILS
    // ─────────────────────────────────────────
    @PutMapping("/{id}")
    public RestaurantResponseDto updateRestaurant(
            @PathVariable Long id,
            @RequestBody RestaurantRequestDto request,
            @RequestHeader("Authorization") String token) {

        return restaurantService.updateRestaurant(id, request, token);
    }

    // ─────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────
    @DeleteMapping("/{id}")
    public String deleteRestaurant(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {

        restaurantService.deleteRestaurant(id, token);
        return "Restaurant deleted successfully";
    }

    // ─────────────────────────────────────────
    //  UPLOAD IMAGE  ← NEW
    //
    //  POST /api/restaurants/{id}/image
    //  Content-Type: multipart/form-data
    //  Body param:   file = <the image>
    //  Header:       Authorization: Bearer <token>
    //
    //  What happens inside:
    //  1. Validate file is not empty
    //  2. Create images/ folder if it doesn't exist yet
    //  3. Generate unique filename  →  restaurant_5_uuid.jpg
    //  4. Write actual bytes to disk  →  images/restaurant_5_uuid.jpg
    //  5. Save path string "/images/restaurant_5_uuid.jpg" into DB
    //  6. Return updated restaurant JSON with imagePath filled
    // ─────────────────────────────────────────
    @PostMapping(value = "/{id}/image", consumes = "multipart/form-data")
    public ResponseEntity<RestaurantResponseDto> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token) throws IOException {

        // Step 1: Reject empty file immediately
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Step 2: Create images/ folder on disk if it doesn't exist
        File imageDir = new File(IMAGE_DIR);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        // Step 3: Build a unique filename so files never overwrite each other
        //         e.g.  restaurant_5_f3a9b1c2-4d5e-....jpg
        String original  = file.getOriginalFilename();
        String extension = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf("."))
                : ".jpg";
        String uniqueFileName = "restaurant_" + id + "_" + UUID.randomUUID() + extension;

        // Step 4: Write the actual image bytes to disk
        Path filePath = Paths.get(IMAGE_DIR + uniqueFileName);
        Files.write(filePath, file.getBytes());

        // Step 5: This string is what gets stored in the database column image_path
        //         It is a URL path, not a filesystem path
        //         Frontend will use it as:  <img src="/images/restaurant_5_uuid.jpg" />
        String relativePath = "/images/" + uniqueFileName;

        // Step 6: Tell service to save relativePath into DB and return updated DTO
        RestaurantResponseDto updated =
                restaurantService.updateRestaurantImage(id, relativePath, token);

        return ResponseEntity.ok(updated);
    }
}