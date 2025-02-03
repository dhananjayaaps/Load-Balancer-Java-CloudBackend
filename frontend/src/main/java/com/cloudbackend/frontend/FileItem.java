package com.cloudbackend.frontend;

import java.util.List;

public class FileItem {
    private String path;
    private List<String> permissions;
    private boolean isDirectory;

    // Constructor
    public FileItem(String path, List<String> permissions, boolean isDirectory) {
        this.path = path;
        this.permissions = permissions;
        this.isDirectory = isDirectory;
    }

    // Getters and Setters
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    // Check if the user has read permission
    public boolean canRead() {
        return permissions.contains("r");
    }

    // Check if the user has write permission
    public boolean canWrite() {
        return permissions.contains("w");
    }
}