package com.anup.spring_advance_assignment.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.anup.spring_advance_assignment.dto.TodoDTO;
import com.anup.spring_advance_assignment.entity.Todo;
import com.anup.spring_advance_assignment.exception.InvalidStatusException;
import com.anup.spring_advance_assignment.exception.ResourceNotFoundException;
import com.anup.spring_advance_assignment.repository.TodoRepository;
import com.anup.spring_advance_assignment.entity.Status;

@Service
public class TodoServiceImpl implements TodoService {
    private TodoRepository todoRepository;

    public TodoServiceImpl(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    private boolean isValidTransition(Status current, Status newStatus) {
        return (current == Status.PENDING && newStatus == Status.COMPLETED)
                || (current == Status.COMPLETED && newStatus == Status.PENDING);
    }

    private TodoDTO mapToDTO(Todo todo) {
        TodoDTO dto = new TodoDTO();
        dto.setTitle(todo.getTitle());
        dto.setDescription(todo.getDescription());
        dto.setStatus(todo.getStatus());
        return dto;
    }

    @Override
    public String createTodo(TodoDTO dto) {
        Todo todo = new Todo();

        todo.setTitle(dto.getTitle());
        todo.setDescription(dto.getDescription());
        todo.setStatus(dto.getStatus());

        if (dto.getStatus() == null) {
            todo.setStatus(Status.PENDING);
        } else {
            todo.setStatus(dto.getStatus());
        }

        todoRepository.save(todo);
        return "Todo created successfully";

    }

    

}
