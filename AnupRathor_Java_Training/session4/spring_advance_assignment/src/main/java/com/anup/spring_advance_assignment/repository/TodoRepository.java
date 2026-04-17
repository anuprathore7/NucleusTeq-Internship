package com.anup.spring_advance_assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.anup.spring_advance_assignment.entity.Todo;

public interface TodoRepository extends JpaRepository<Todo , Long> {
    // Define repository methods here
}