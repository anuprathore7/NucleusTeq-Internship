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

/**
 * This controller handles all restaurant-related operations.
 * It allows owners to create, update, and delete restaurants,
 * while customers can view and browse available restaurants.
 * It also supports uploading images for restaurants.
 */
@RestController
@RequestMapping(RestaurantController.BASE_URL)
public class RestaurantController {
    public static final String BASE_URL="/api/restaurants";
    public static final String OWNER="/owner";
    public static final String BY_ID="/{id}";
    public static final String IMAGE="/{id}/image";
    private final RestaurantService restaurantService;
    private static final String IMAGE_DIR="images/";
    public RestaurantController(RestaurantService restaurantService){this.restaurantService=restaurantService;}
    /**
     * Creates a new restaurant for the logged-in owner.
     * Requires valid authentication and restaurant details in request body.
     */
    @PostMapping
    public RestaurantResponseDto createRestaurant(@RequestBody RestaurantRequestDto request,@RequestHeader("Authorization") String token){
        return restaurantService.createRestaurant(request,token);
    }
    /**
     * Returns all restaurants available in the system.
     * This is mainly used by customers to browse restaurants.
     */
    @GetMapping
    public List<RestaurantResponseDto> getAll(){
        return restaurantService.getAllRestaurants();
    }
    /**
     * Returns all restaurants owned by the logged-in user.
     * Useful for owner dashboard to manage their restaurants.
     */
    @GetMapping(OWNER)
    public List<RestaurantResponseDto> getMyRestaurants(@RequestHeader("Authorization") String token){
        return restaurantService.getRestaurantsByOwnerToken(token);
    }
    /**
     * Fetches details of a single restaurant by its ID.
     */
    @GetMapping(BY_ID)
    public RestaurantResponseDto getById(@PathVariable Long id){
        return restaurantService.getRestaurantById(id);
    }
    /**
     * Updates details of an existing restaurant.
     * Only the owner of the restaurant is allowed to perform this action.
     */
    @PutMapping(BY_ID)
    public RestaurantResponseDto updateRestaurant(@PathVariable Long id,@RequestBody RestaurantRequestDto request,@RequestHeader("Authorization") String token){
        return restaurantService.updateRestaurant(id,request,token);
    }
    /**
     * Deletes a restaurant by its ID.
     * This action is restricted to the owner of the restaurant.
     */
    @DeleteMapping(BY_ID)
    public String deleteRestaurant(@PathVariable Long id,@RequestHeader("Authorization") String token){
        restaurantService.deleteRestaurant(id,token);
        return "Restaurant deleted successfully";
    }
    /**
     * Uploads an image for a restaurant and stores it on disk.
     * Generates a unique file name, saves the file, and updates the database with the image path.
     */
    @PostMapping(value=IMAGE,consumes="multipart/form-data")
    public ResponseEntity<RestaurantResponseDto> uploadImage(@PathVariable Long id,@RequestParam("file") MultipartFile file,@RequestHeader("Authorization") String token) throws IOException{
        if(file.isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        File imageDir=new File(IMAGE_DIR);
        if(!imageDir.exists()){
            imageDir.mkdirs();
        }
        String original=file.getOriginalFilename();
        String extension=(original!=null&&original.contains("."))?original.substring(original.lastIndexOf(".")):".jpg";
        String uniqueFileName="restaurant_"+id+"_"+UUID.randomUUID()+extension;
        Path filePath=Paths.get(IMAGE_DIR+uniqueFileName);
        Files.write(filePath,file.getBytes());
        String relativePath="/images/"+uniqueFileName;
        RestaurantResponseDto updated=restaurantService.updateRestaurantImage(id,relativePath,token);
        return ResponseEntity.ok(updated);
    }
}