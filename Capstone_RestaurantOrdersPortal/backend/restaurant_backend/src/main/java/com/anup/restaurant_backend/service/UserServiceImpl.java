package com.anup.restaurant_backend.service;

import com.anup.restaurant_backend.dto.LoginRequest;
import com.anup.restaurant_backend.dto.UserRequestDto;
import com.anup.restaurant_backend.entity.UserEntity;
import com.anup.restaurant_backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
// This class contains actual implementation of business logic
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // this is database repository and are making Object here .
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder){

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());

        //  Step 3: Apply business rule
        user.setWalletBalance(1000.0);

        //  Step 4: Save to database
        userRepository.save(user);

        return "User registered successfully";
    }

    @Override
    public String loginUser(LoginRequest dto){
        // basically we are checking the dto or user is coming that is valid or not if not then it will handle null
        Optional<UserEntity> existingUser = userRepository.findByEmail(dto.getEmail());
        // It ensures checking of user weather it is empty or not
        if (existingUser.isEmpty()){
            return "User not Found";
        }
        // here It finds the User from the database with the help of existing user which is coming from dto.
        UserEntity user = existingUser.get();
        // here Password checks weather it is correct or not if incorrect then show this or return login successfully.
        if (!passwordEncoder.matches(dto.getPassword() , user.getPassword())){
            return "Invalid email or password";
        }

        return "Login Successfully";
    }
}