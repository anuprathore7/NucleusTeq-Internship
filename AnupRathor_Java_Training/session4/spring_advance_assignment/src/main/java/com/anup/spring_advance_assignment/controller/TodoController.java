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

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@RestController
@RequestMapping("/todos")
public class TodoController {

    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);
    private final TodoService service;

    public TodoController(TodoService service){
        this.service = service;
    }

    @PostMapping()
    public String createTodo(@RequestBody @Valid TodoDTO dto){
        logger.info("Received request to create TODO with title: {}", dto.getTitle());
        return service.createTodo(dto);
    }

    @GetMapping()
    public List<TodoDTO> getAllTodos(){
        logger.info("Received request to fetch all TODOs");
        return service.getAllTodos();
    }

    @GetMapping("/{id}")
    public TodoDTO geTodoDTO(@PathVariable Long id){
        logger.info("Received request to fetch TODO with ID: {}", id);

        return service.getTodoById(id);
    }
    
    @PutMapping("/{id}")
    public String updateTodo(@PathVariable Long id , @RequestBody @Valid TodoDTO dto){
        logger.info("Received request to update TODO with ID: {}", id);
        return service.updateTodo(id, dto);
    }

    @DeleteMapping("/{id}")
    public String deleteTodo(@PathVariable Long id){
        logger.info("Received request to delete TODO with ID: {}", id);
        return service.deleteTodo(id);
    }





    
}
