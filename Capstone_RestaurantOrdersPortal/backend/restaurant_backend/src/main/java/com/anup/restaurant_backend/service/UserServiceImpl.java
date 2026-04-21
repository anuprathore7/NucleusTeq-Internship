package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.UserRequestDto;
import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
// This class contains actual implementation of business logic
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    // this is database repository and are making Object here .
    public UserServiceImpl(UserRepository userRepository){

        this.userRepository = userRepository;
    }


    @Override
    public String registerUser(UserRequestDto dto) {

        //  Step 1: Check if user already exists
        Optional<UserEntity> existingUser = userRepository.findByEmail(dto.getEmail());

        if (existingUser.isPresent()) {
            return "Email already registered";
        }

        //  Step 2: Convert DTO → Entity
        UserEntity user = new UserEntity();

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());

        //  Step 3: Apply business rule
        user.setWalletBalance(1000.0);

        //  Step 4: Save to database
        userRepository.save(user);

        return "User registered successfully";
    }
}