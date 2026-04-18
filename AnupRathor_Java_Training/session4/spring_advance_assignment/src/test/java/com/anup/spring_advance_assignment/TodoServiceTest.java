
package com.anup.spring_advance_assignment;

import com.anup.spring_advance_assignment.service.NotificationService;
import com.anup.spring_advance_assignment.service.TodoServiceImpl;
import com.anup.spring_advance_assignment.dto.TodoDTO;
import com.anup.spring_advance_assignment.entity.Status;
import com.anup.spring_advance_assignment.entity.Todo;
import com.anup.spring_advance_assignment.exception.InvalidStatusException;
import com.anup.spring_advance_assignment.exception.ResourceNotFoundException;
import com.anup.spring_advance_assignment.repository.TodoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository repository;

    @Mock
    private NotificationService notificationClient;

    @InjectMocks
    private TodoServiceImpl service;


    // CREATE TEST
    @Test
    void testCreateTodo() {
        TodoDTO dto = new TodoDTO();
        dto.setTitle("Test Todo");

        when(repository.save(any(Todo.class))).thenReturn(new Todo());

        String result = service.createTodo(dto);

        assertEquals("Todo created successfully", result);
        verify(notificationClient).sendNotification(anyString());
    }

     //  GET ALL TEST
    @Test
    void testGetAllTodos() {
        Todo t1 = new Todo();
        t1.setTitle("Task1");

        Todo t2 = new Todo();
        t2.setTitle("Task2");

        when(repository.findAll()).thenReturn(Arrays.asList(t1, t2));

        List<TodoDTO> result = service.getAllTodos();

        assertEquals(2, result.size());
    }

    //  GET BY ID SUCCESS
    @Test
    void testGetTodoById_Success() {
        Todo todo = new Todo();
        todo.setTitle("Test");

        when(repository.findById(1L)).thenReturn(Optional.of(todo));

        TodoDTO result = service.getTodoById(1L);

        assertEquals("Test", result.getTitle());
    }

    //  GET BY ID NOT FOUND
    @Test
    void testGetTodoById_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getTodoById(1L));
    }

      //  UPDATE SUCCESS
    @Test
    void testUpdateTodo_Success() {
        Todo existing = new Todo();
        existing.setStatus(Status.PENDING);

        TodoDTO dto = new TodoDTO();
        dto.setTitle("Updated");
        dto.setStatus(Status.COMPLETED);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        String result = service.updateTodo(1L, dto);

        assertEquals("Todo updated successfully", result);
    }

    //  UPDATE INVALID STATUS
    @Test
    void testUpdateTodo_InvalidStatus() {
        Todo existing = new Todo();
        existing.setStatus(Status.PENDING);

        TodoDTO dto = new TodoDTO();
        dto.setStatus(Status.PENDING); // invalid transition

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(InvalidStatusException.class,
                () -> service.updateTodo(1L, dto));
    }

    //  UPDATE NOT FOUND
    @Test
    void testUpdateTodo_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        TodoDTO dto = new TodoDTO();

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateTodo(1L, dto));
    }

    
    
}
