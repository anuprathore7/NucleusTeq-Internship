package com.anup.springboot_project.controller;

import com.anup.springboot_project.service.NotificationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notify")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public String notifyUser() {
        return service.trigger();
    }
}