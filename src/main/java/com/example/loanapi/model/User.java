package com.example.loanapi.model;

/**
 * Model class representing a user in the system
 * Immutable entity used for authentication and authorization
 */
public class User {

    private final Long id;
    private final String name;
    private final String email;
    private final UserRole role;
    private final String token;

    public User(Long id, String name, String email, UserRole role, String token) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}
