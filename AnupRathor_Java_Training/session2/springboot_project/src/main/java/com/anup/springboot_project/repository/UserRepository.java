package com.anup.springboot_project.repository;

import com.anup.springboot_project.model.User;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UserRepository {

    private final Map<Integer, User> userMap = new HashMap<>();

    public List<User> findAll() {
        return new ArrayList<>(userMap.values());
    }

    public void save(User user) {
        userMap.put(user.getId(), user);
    }

    public Optional<User> findById(int id) {
        return Optional.ofNullable(userMap.get(id));
    }
}