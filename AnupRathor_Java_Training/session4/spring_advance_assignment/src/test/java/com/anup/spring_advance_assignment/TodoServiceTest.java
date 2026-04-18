
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

    
}
