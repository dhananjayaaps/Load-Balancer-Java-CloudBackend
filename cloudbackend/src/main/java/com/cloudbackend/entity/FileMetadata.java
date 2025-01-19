package com.cloudbackend.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // File name

    private int totalChunks;

    @Column(nullable = false)
    private String path; // Path to the file in the storage container

    @Column(nullable = false)
    private Long size; // File size in bytes

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner; // The user who owns this file

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FilePermission> permissions; // List of permissions for this file

    public FileMetadata() {
    }

    public FileMetadata(String name, String path, Long size, User owner) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<FilePermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<FilePermission> permissions) {
        this.permissions = permissions;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(int totalChunks) {
        this.totalChunks = totalChunks;
    }
}
