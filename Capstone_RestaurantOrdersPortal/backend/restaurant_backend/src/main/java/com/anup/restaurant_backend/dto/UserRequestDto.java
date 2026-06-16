package com.anup.restaurant_backend.dto;

import com.anup.restaurant_backend.enums.UserRole;

/**
 * Request object used during user registration.
 * Contains all necessary details required to create a new user account.
 */
public class UserRequestDto {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phone;
    private UserRole role;

    /**
     * Returns first name.
     */
    public String getFirstName() { return firstName; }

    /**
     * Sets first name.
     */
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /**
     * Returns last name.
     */
    public String getLastName() { return lastName; }

    /**
     * Sets last name.
     */
    public void setLastName(String lastName) { this.lastName = lastName; }

    /**
     * Returns email.
     */
    public String getEmail() { return email; }

    /**
     * Sets email.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns password.
     */
    public String getPassword() { return password; }

    /**
     * Sets password.
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Returns phone.
     */
    public String getPhone() { return phone; }

    /**
     * Sets phone.
     */
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * Returns user role.
     */
    public UserRole getRole() { return role; }

    /**
     * Sets user role.
     */
    public void setRole(UserRole role) { this.role = role; }
}