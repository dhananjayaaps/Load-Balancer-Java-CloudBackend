package com.cloudbackend.frontend;

public class Role {
    private Long id;
    private String name;
    private String role; // This is what we need

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
