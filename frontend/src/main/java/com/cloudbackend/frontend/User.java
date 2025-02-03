package com.cloudbackend.frontend;

public class User {
    private Long id;
    private String username;

    // Constructor
    public User(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}