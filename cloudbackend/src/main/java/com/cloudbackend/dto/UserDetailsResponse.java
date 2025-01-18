package com.cloudbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailsResponse {
    private String username;
    public UserDetailsResponse() {
    }

    public UserDetailsResponse(String username, String name, String email, boolean success) {
        this.username = username;
    }

}
