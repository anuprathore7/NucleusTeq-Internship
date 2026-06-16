package com.anup.restaurant_backend.dto;

/**
 *  Output → JWT token
 */
public class AuthResponse {

    private String token;

    public AuthResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}