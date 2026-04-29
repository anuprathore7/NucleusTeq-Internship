package com.anup.restaurant_backend.dto;


public class LoginRequest{
    private String email;
    private String password;

    // Getters and Setters

    public void setEmail(String email){
        this.email = email;
    }

    public String getEmail (){
        return email;
    }

    public void setPassword (String password){
        this.password = password;
    }

    public String getPassword() { return password; }


}
