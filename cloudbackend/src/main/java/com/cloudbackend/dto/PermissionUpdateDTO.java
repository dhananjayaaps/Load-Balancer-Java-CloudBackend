package com.cloudbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionUpdateDTO {
    private Long userId;
    private boolean canRead;
    private boolean canWrite;

    // getters and setters
}