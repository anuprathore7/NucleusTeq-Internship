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

/**
 * This controller manages authentication related tasks.
 * It handles user login by verifying credentials and generating a JWT token,
 * and also allows new users to register into the system.
 */
@RestController
@RequestMapping(AuthController.BASE_URL)
public class AuthController {
    public static final String BASE_URL="/api/auth";
    public static final String LOGIN="/login";
    public static final String REGISTER="/register";
    private static final Logger log=LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;
    public AuthController(AuthenticationManager authenticationManager,JwtService jwtService,UserService userService,UserRepository userRepository){
        this.authenticationManager=authenticationManager;
        this.jwtService=jwtService;
        this.userService=userService;
        this.userRepository=userRepository;
    }
    /**
     * Authenticates the user using email and password.
     * If credentials are valid, a JWT token is generated and returned.
     */
    @PostMapping(LOGIN)
    public AuthResponse login(@RequestBody LoginRequest request){
        log.info("Login request received for email: {}",request.getEmail());
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
        }catch(BadCredentialsException e){
            log.warn("Login failed — bad credentials for email: {}",request.getEmail());
            throw e;
        }
        UserEntity user=userRepository.findByEmail(request.getEmail()).get();
        String token=jwtService.generateToken(request.getEmail(),user.getRole().name());
        return new AuthResponse(token);
    }
    /**
     * Registers a new user with the provided details.
     * Returns a message indicating success or failure.
     */
    @PostMapping(REGISTER)
    public String register(@RequestBody UserRequestDto request){
        return userService.registerUser(request);
    }
}