package com.anup.springboot_project.service;

import com.anup.springboot_project.component.NotificationComponent;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationComponent component;

    public NotificationService(NotificationComponent component) {
        this.component = component;
    }

    public String trigger() {
        return component.send();
    }
}