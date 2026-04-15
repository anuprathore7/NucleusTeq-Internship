package com.anup.spring_and_rest_assignment.repository;

import com.anup.spring_and_rest_assignment.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepository {

    private List<User> users = new ArrayList<>();

    public UserRepository() {
        users.add(new User(1L, "Anup", 21, "USER"));
        users.add(new User(2L, "Anshika", 22, "ADMIN"));
        users.add(new User(3L, "Aman", 22, "USER"));
        users.add(new User(4L, "Alfez", 21, "USER"));
        users.add(new User(5L, "Ravi", 23, "ADMIN"));
    }

    public List<User> getAllUsers() {
        return users;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void deleteUser(Long id) {
        users.removeIf(user -> user.getId().equals(id));
    }
}