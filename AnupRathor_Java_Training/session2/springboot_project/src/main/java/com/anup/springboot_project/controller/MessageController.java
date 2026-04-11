package com.anup.springboot_project.controller;

import com.anup.springboot_project.service.MessageService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
public class MessageController {

    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    @GetMapping
    public String getMessage(@RequestParam String type) {
        return service.getMessage(type);
    }
}