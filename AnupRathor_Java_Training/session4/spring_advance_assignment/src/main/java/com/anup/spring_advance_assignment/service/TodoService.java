package com.anup.spring_advance_assignment.service;

import com.anup.spring_advance_assignment.dto.TodoDTO;
import java.util.List;

public interface TodoService {

    String createTodo(TodoDTO dto);

    List<TodoDTO> getAllTodos();

    TodoDTO getTodoById(Long id);

    String updateTodo(Long id, TodoDTO dto);

    String deleteTodo(Long id);
}