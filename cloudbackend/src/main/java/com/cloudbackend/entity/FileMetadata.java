package com.cloudbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // File name

    private int totalChunks;

    @Column(nullable = false)
    private String path; // Path to the file in the storage container

    @Column(nullable = false)
    private Long size; // File size in bytes

    @Column(nullable = false)
    private Character mimeType = 'f';

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner; // The user who owns this file

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FilePermission> permissions; // List of permissions for this file

    @Column(nullable = false)
    private boolean othersCanRead = false;

    @Column(nullable = false)
    private boolean othersCanWrite = false;

    @Column(nullable = false)
    private boolean isDirectory = false;

    public FileMetadata() {
    }

    public FileMetadata(String name, String path, Long size, User owner) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.owner = owner;
    }

    public FileMetadata(String name, String path, Long size, User owner,
                        boolean othersCanRead, boolean othersCanWrite) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.owner = owner;
        this.othersCanRead = othersCanRead;
        this.othersCanWrite = othersCanWrite;
    }

    public FileMetadata(String name, String path, Long size, User owner,
                        boolean othersCanRead, boolean othersCanWrite, boolean isDirectory) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.owner = owner;
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
