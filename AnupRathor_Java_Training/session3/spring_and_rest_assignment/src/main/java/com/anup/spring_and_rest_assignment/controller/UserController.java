
package com.anup.spring_and_rest_assignment.controller;

import com.anup.spring_and_rest_assignment.model.User;
import com.anup.spring_and_rest_assignment.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // This tells Spring: this class will handle REST APIs (returns JSON directly)
public class UserController {

    // We keep it final because dependency should not change
    private final UserService userService;

    // Constructor Injection → Spring automatically gives UserService object here
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET API → used to search users with optional filters
    @GetMapping("/users/search")
    public ResponseEntity<List<User>> searchUsers(

            // These are optional query parameters (user may or may not send them)
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String role) {

        // We simply pass data to service and return the result
        return ResponseEntity.ok(
                userService.searchUsers(name, age, role)
        );
    }

       // POST API → used to submit (add) a new user
    @PostMapping("/submit")
    public ResponseEntity<String> submitUser(

            // JSON request body will be converted into User object automatically
            @RequestBody User user) {

        // Call service layer to handle logic and validation
        userService.submitUser(user);

        // Return 201 status (created successfully)
        return ResponseEntity.status(201)
                .body("User added successfully");
    }


    

   
}