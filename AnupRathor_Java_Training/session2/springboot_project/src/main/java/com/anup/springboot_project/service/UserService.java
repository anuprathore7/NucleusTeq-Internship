package com.anup.springboot_project.service;

import com.anup.springboot_project.exception.UserNotFoundException;
import com.anup.springboot_project.model.User;
import com.anup.springboot_project.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public String createUser(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            return "Name cannot be empty";
        }
        repository.save(user);
        return "User created successfully";
    }

    public User getUserById(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}