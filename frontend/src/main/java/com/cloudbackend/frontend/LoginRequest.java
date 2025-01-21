package com.cloudbackend.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

class LoginRequest {
    @JsonProperty
    private String username;
    @JsonProperty
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}