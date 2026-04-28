package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.*;
import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.repository.UserRepository;
import com.anup.restaurant_backend.security.JwtService;
import com.anup.restaurant_backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                          UserService userService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            log.warn("Login failed — bad credentials for email: {}", request.getEmail());
            throw e;  // Let Spring handle the 401
        }

        UserEntity user = userRepository.findByEmail(request.getEmail()).get();
        log.info("Authentication passed, generating JWT for: {}", request.getEmail());

        String token = jwtService.generateToken(request.getEmail(), user.getRole().name());

        log.info("JWT generated successfully for: {}", request.getEmail());
        return new AuthResponse(token);
    }

    @PostMapping("/register")
    public String register(@RequestBody UserRequestDto request) {
        log.info("Register request received for email: {}", request.getEmail());

        String result = userService.registerUser(request);

        if ("User registered successfully".equals(result)) {
            log.info("Registration successful for: {}", request.getEmail());
        } else {
            log.warn("Registration rejected for: {} — reason: {}", request.getEmail(), result);
        }

        return result;
    }
}