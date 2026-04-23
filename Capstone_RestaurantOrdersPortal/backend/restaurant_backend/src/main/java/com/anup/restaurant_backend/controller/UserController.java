package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.LoginRequest;
import com.anup.restaurant_backend.dto.UserRequestDto;
import com.anup.restaurant_backend.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/profile")
    public String getProfile() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName(); // comes from JWT

        return "Logged in user: " + email;
    }
}