package com.anup.springboot_project.component;

import org.springframework.stereotype.Component;

@Component("SHORT")
public class ShortMessageFormatter implements MessageFormatter {
    public String format() {
        return "Short Message";
    }
}