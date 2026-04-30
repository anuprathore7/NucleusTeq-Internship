package com.anup.restaurant_backend.dto;

/**
 * Request object used during user login.
 * It carries the email and password entered by the user
 * which are then used for authentication.
 */
public class LoginRequest{
    private String email;
    private String password;

    /**
     * Sets user email.
     */
    public void setEmail(String email){
        this.email = email;
    }

    /**
     * Returns user email.
     */
    public String getEmail (){
        return email;
    }

    /**
     * Sets user password.
     */
    public void setPassword (String password){
        this.password = password;
    }

    /**
     * Returns user password.
     */
    public String getPassword() { return password; }
}