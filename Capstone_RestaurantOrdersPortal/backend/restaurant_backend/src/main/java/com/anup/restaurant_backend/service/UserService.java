package com.anup.restaurant_backend.service;


import com.anup.restaurant_backend.dto.LoginRequest;
import com.anup.restaurant_backend.dto.UserRequestDto;

public interface UserService {

    String registerUser(UserRequestDto dto);

    String loginUser(LoginRequest dto);
}