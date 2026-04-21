package com.anup.restaurant_backend.controller;

import com.anup.restaurant_backend.dto.UserRequestDto;
import com.anup.restaurant_backend.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/register")
    public String userRegister(@RequestBody UserRequestDto dto){
        return userService.registerUser(dto);
    }




}