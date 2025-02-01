package com.cloudbackend.frontend;

import java.util.List;

public class FileItem {
    private String path;
    private List<String> permissions;
    private boolean isDirectory;

    public FileItem(String path, List<String> permissions, boolean isDirectory) {
        this.path = path;
        this.permissions = permissions;
        this.isDirectory = isDirectory;
    }

    public String getPath() {
        return path;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    @Override
    public String toString() {
        return path;
    }
}