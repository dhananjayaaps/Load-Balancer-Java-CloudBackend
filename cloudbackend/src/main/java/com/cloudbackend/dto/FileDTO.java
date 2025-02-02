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

}