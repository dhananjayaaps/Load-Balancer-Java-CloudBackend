package com.cloudbackend.frontend;

import java.util.List;
import java.util.stream.Collectors;

public class User {
    private Long id;
    private String name;
    private String username;
    private List<Role> roles; // Ensure this is a List<Role>

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; }

    // Convert list of roles to a string
    public String getRolesAsString() {
        if (roles == null || roles.isEmpty()) {
            return "No Role";
        }
        return roles.stream()
                .map(Role::getRole) // Fetch "USER" or "ADMIN" from role object
                .collect(Collectors.joining(", "));
    }
}
