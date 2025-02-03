package com.cloudbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionUpdateDTO {
    private Long userId;
    private boolean canRead;
    private boolean canWrite;

    public Long getUserId() {
        return userId;
    }

    void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isCanRead() {
        return canRead;
    }

    void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public boolean isCanWrite() {
        return canWrite;
    }

    void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }


}