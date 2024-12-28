package com.cloudbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String username;
    private String role;

    public UserDTO(String name, String nic, String username, String email, String role) {
        this.username = username;
        this.role = role;
    }

}
