package com.anup.spring_advance_assignment.controller;
import java.util.*;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.anup.spring_advance_assignment.dto.TodoDTO;
import com.anup.spring_advance_assignment.service.TodoService;



@RestController
@RequestMapping("/todos")
public class TodoController {

    private final TodoService service;

    public TodoController(TodoService service){
        this.service = service;
    }

    @PostMapping()
    public String createTodo(@RequestBody TodoDTO dto){
        return service.createTodo(dto);
    }

    @GetMapping()
    public List<TodoDTO> getAllTodos(){
        return service.getAllTodos();
    }

    @GetMapping("/{id}")
    public TodoDTO geTodoDTO(@PathVariable Long id){
        return service.getTodoById(id);
    }
    
    @PutMapping("/{id}")
    public String updateTodo(@PathVariable Long id , @RequestBody TodoDTO dto){
        return service.updateTodo(id, dto);
    }

    @DeleteMapping("/{id}")
    public String deleteTodo(@PathVariable Long id){
        return service.deleteTodo(id);
    }





    
}
