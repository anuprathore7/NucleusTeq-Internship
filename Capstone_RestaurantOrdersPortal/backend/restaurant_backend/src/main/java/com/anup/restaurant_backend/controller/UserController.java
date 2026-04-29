package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * UserController
 * GET /api/users/profile → returns full user profile + wallet balance
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public Map<String, Object> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("id",            user.getId());
        response.put("firstName",     user.getFirstName());
        response.put("lastName",      user.getLastName());
        response.put("email",         user.getEmail());
        response.put("phone",         user.getPhone());
        response.put("role",          user.getRole().name());
        response.put("walletBalance", user.getWalletBalance());

        return response;
    }
}