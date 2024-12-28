package com.cloudbackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class LoginResponse {
    private String token;
    private List<String> roles;

    public LoginResponse(String jwt, List<String> roles) {
        this.token = jwt;
        this.roles = roles;
    }
}
