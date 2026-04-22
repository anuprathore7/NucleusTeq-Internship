package com.anup.restaurant_backend.service;


import com.anup.restaurant_backend.dto.LoginRequest;
import com.anup.restaurant_backend.dto.UserRequestDto;
// This is implement class where we only define methods but do not derieved and that will be derived in inherited classes
public interface UserService {

    String registerUser(UserRequestDto dto);

    String loginUser(LoginRequest dto);
}