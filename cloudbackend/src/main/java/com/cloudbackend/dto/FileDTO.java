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

    public FileDTO(String path, Long size, boolean b, boolean b1, boolean othersCanRead, boolean othersCanWrite) {
        this.path = path;
        this.size = size;
        this.canRead = b;
        this.canWrite = b1;
        this.othersCanRead = othersCanRead;
        this.othersCanWrite = othersCanWrite;
    }
}