package com.cloudbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileDTO {
    private String path;
    private Long size;
    private boolean canRead;
    private boolean canWrite;
    private boolean othersCanRead;
    private boolean othersCanWrite;
    private boolean isDirectory;

    // Constructor, getters, and setters
    public FileDTO(String path, Long size, boolean canRead, boolean canWrite,
                   boolean othersCanRead, boolean othersCanWrite, boolean isDirectory) {
        this.path = path;
        this.size = size;
        this.canRead = canRead;
        this.canWrite = canWrite;
        this.othersCanRead = othersCanRead;
        this.othersCanWrite = othersCanWrite;
        this.isDirectory = isDirectory;
    }

    // Add getter and setter for isDirectory
    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }
}