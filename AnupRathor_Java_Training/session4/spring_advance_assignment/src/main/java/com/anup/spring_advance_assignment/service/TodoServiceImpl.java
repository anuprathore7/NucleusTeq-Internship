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

    @Override
    public List<TodoDTO> getAllTodos() {
        return todoRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TodoDTO getTodoById(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));

        return mapToDTO(todo);
    }

    @Override
    public String updateTodo(Long id, TodoDTO dto) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found"));

        if (dto.getStatus() != null) {
            if (!isValidTransition(todo.getStatus(), dto.getStatus())) {
                throw new InvalidStatusException("Invalid status transition");
            }
            todo.setStatus(dto.getStatus());
        }

        todo.setTitle(dto.getTitle());
        todo.setDescription(dto.getDescription());

        todoRepository.save(todo);

        return "Todo updated successfully";

    }

    @Override
    public String deleteTodo(Long id) {
        Todo todo = todoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Todo not found"));

        todoRepository.delete(todo);
        return "Todo deleted successfully";
    }

}
