package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.*;
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

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService , UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
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
        String token = jwtService.generateToken(request.getEmail());

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