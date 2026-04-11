package com.anup.springboot_project.component;

import org.springframework.stereotype.Component;

@Component
public class NotificationComponent {

    public String send() {
        return "Notification sent";
    }
}