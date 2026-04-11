package com.anup.springboot_project.service;

import com.anup.springboot_project.component.MessageFormatter;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MessageService {

    private final Map<String, MessageFormatter> formatterMap;

    public MessageService(Map<String, MessageFormatter> formatterMap) {
        this.formatterMap = formatterMap;
    }

    public String getMessage(String type) {
        MessageFormatter formatter = formatterMap.get(type);

        if (formatter == null) {
            return "Invalid type. Use SHORT or LONG";
        }

        return formatter.format();
    }
}