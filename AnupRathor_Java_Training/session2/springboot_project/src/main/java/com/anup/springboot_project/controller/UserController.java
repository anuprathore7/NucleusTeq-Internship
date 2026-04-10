package com.anup.springboot_project.controller;

import com.anup.springboot_project.model.User;
import com.anup.springboot_project.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<User> getUsers() {
        return service.getAllUsers();
    }

    @PostMapping
    public String createUser(@RequestBody User user) {
        return service.createUser(user);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable int id) {
        return service.getUserById(id);
    }
}