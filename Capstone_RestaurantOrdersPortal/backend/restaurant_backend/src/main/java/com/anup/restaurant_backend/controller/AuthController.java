package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.*;
import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.repository.UserRepository;
import com.anup.restaurant_backend.security.JwtService;
import com.anup.restaurant_backend.service.UserService;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService , UserService userService , UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     *  LOGIN API
     */
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {

        /**
         * Validate email + password
         */
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        /**
         * Generate JWT
         */
        // First fetch user to get role
        UserEntity user = userRepository.findByEmail(request.getEmail()).get();
        String token = jwtService.generateToken(request.getEmail(), user.getRole().name());

        /**
         * Return token
         */
        return new AuthResponse(token);
    }

    @PostMapping("/register")
    public String register (@RequestBody UserRequestDto request) {
        return userService.registerUser(request);
    }
}