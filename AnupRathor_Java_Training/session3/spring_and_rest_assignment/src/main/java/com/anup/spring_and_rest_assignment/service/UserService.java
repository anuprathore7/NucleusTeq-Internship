package com.anup.spring_and_rest_assignment.service;

import com.anup.spring_and_rest_assignment.model.User;
import com.anup.spring_and_rest_assignment.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service // Marks this class as the "business logic" layer (Spring will manage it)
public class UserService {

    // We are using final because this dependency should not change once assigned
    private final UserRepository userRepository;

    // Constructor Injection → Spring will automatically provide UserRepository here
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // This method handles searching users based on optional filters
    public List<User> searchUsers(String name, Integer age, String role) {

        // First we fetch all users from repository (like getting full data from DB)
        List<User> users = userRepository.getAllUsers();

        // Now we apply filtering based on given parameters
        return users.stream()
                .filter(user ->
                        // If name is not provided → ignore it
                        // If provided → match ignoring uppercase/lowercase
                        (name == null || user.getName().equalsIgnoreCase(name)) &&

                        // If age is not provided → ignore it
                        // If provided → match exactly
                        (age == null || user.getAge().equals(age)) &&

                        // Same logic for role (case-insensitive match)
                        (role == null || user.getRole().equalsIgnoreCase(role))
                )
                // Convert filtered result back to List
                .collect(Collectors.toList());
    }

}