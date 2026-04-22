package com.anup.restaurant_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration // This class gives configuration to Spring Boot.
public class PasswordConfig {

    @Bean // Create the object of BCryptPasswordEncoder and manage it for me
    public BCryptPasswordEncoder passwordEncoder() {
        // this is actual encryption tool that hashes password and adds security and provides decryption as well.
        return new BCryptPasswordEncoder();
    }
}