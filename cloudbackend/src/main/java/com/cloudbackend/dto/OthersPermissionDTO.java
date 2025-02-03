package com.cloudbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OthersPermissionDTO {
    private boolean othersCanRead;
    private boolean othersCanWrite;

    public OthersPermissionDTO() {
        this.othersCanRead = false;
        this.othersCanWrite = false;
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

}