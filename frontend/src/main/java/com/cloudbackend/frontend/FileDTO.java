package com.cloudbackend.frontend;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileDTO {
    private String path;
    private Long size;
    private boolean canRead;
    private boolean canWrite;
    private boolean othersCanRead;
    private boolean othersCanWrite;
    private boolean isDirectory;

    public FileDTO() {}

    // Constructor
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

    // Getters and Setters
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

    public boolean isCanRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public boolean isCanWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

    public boolean isOthersCanRead() {
        return othersCanRead;
    }

    public void setOthersCanRead(boolean othersCanRead) {
        this.othersCanRead = othersCanRead;
    }

    public boolean isOthersCanWrite() {
        return othersCanWrite;
    }

    public void setOthersCanWrite(boolean othersCanWrite) {
        this.othersCanWrite = othersCanWrite;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }
}