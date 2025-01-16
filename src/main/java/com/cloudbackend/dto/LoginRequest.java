package com.cloudbackend.dto;

import lombok.Getter;
import lombok.Setter;

public class LoginRequest {
    // Getters and Setters
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
