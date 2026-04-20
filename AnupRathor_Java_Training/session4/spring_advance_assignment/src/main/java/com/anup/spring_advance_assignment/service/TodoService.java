package com.anup.spring_advance_assignment.service;

import com.anup.spring_advance_assignment.dto.TodoDTO;
import java.util.List;
// This is the Todo Service Interface it means any class will be inherited from this in that class should have these methods
public interface TodoService {

    String createTodo(TodoDTO dto);

    List<TodoDTO> getAllTodos();

    TodoDTO getTodoById(Long id);

    String updateTodo(Long id, TodoDTO dto);

    String deleteTodo(Long id);
}