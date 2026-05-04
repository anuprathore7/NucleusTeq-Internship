package com.anup.restaurant_backend.controller;
import com.anup.restaurant_backend.dto.AddressRequestDto;
import com.anup.restaurant_backend.dto.AddressResponseDto;
import com.anup.restaurant_backend.service.AddressService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * This controller handles all operations related to user addresses.
 * It allows a logged-in user to add new addresses, view saved ones,
 * and delete any address that belongs to them.
 */
@RestController
@RequestMapping(AddressController.BASE_URL)
public class AddressController {
    public static final String BASE_URL="/api/addresses";
    private final AddressService addressService;
    public AddressController(AddressService addressService){this.addressService=addressService;}
    /**
     * Returns all addresses saved by the currently logged-in user.
     * The user is identified using the Authorization token.
     */
    @GetMapping
    public List<AddressResponseDto> getMyAddresses(@RequestHeader("Authorization") String token){
        return addressService.getMyAddresses(token);
    }
    /**
     * Adds a new address for the logged-in user.
     * The address details are taken from the request body.
     */
    @PostMapping
    public AddressResponseDto addAddress(@RequestBody AddressRequestDto request,@RequestHeader("Authorization") String token){
        return addressService.addAddress(request,token);
    }
    /**
     * Deletes an address by its ID if it belongs to the user.
     * Prevents users from deleting others' addresses.
     */
    @DeleteMapping("/{id}")
    public String deleteAddress(@PathVariable Long id,@RequestHeader("Authorization") String token){
        addressService.deleteAddress(id,token);
        return "Address deleted successfully";
    }
}