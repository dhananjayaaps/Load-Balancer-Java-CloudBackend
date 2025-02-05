package com.cloudbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRequest {
    private String path;
    private String fileName;
    private String content;

    // Getters and Setters
}
