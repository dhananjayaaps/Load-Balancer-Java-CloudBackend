package com.cloudbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermssionRequest {
    private String path;
    private boolean canRead;
    private  boolean canWrite;

    public void PermissionRequest(String path, boolean canRead, boolean canWrite) {
        this.path = path;
        this.canRead = canRead;
        this.canWrite = canWrite;
    }
}
