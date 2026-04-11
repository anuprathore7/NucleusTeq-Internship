package com.anup.springboot_project.component;

import org.springframework.stereotype.Component;

@Component("LONG")
public class LongMessageFormatter implements MessageFormatter {
    public String format() {
        return "This is a long formatted message";
    }
}