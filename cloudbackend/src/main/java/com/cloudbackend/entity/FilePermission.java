package com.cloudbackend.entity;

import jakarta.persistence.*;

@Entity
public class FilePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileMetadata file; // The file this permission applies to

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The user this permission is granted to

    @Column(nullable = false)
    private boolean canRead = false; // Whether the user can read the file

    @Column(nullable = false)
    private boolean canWrite = false; // Whether the user can write to the file

    // Constructors
    public FilePermission() {
    }

    public FilePermission(FileMetadata file, User user, boolean canRead, boolean canWrite) {
        this.file = file;
        this.user = user;
        this.canRead = canRead;
        this.canWrite = canWrite;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FileMetadata getFile() {
        return file;
    }

    public void setFile(FileMetadata file) {
        this.file = file;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilePermission that = (FilePermission) o;

        if (!file.equals(that.file)) return false;
        return user.equals(that.user);
    }

    @Override
    public int hashCode() {
        int result = file.hashCode();
        result = 31 * result + user.hashCode();
        return result;
    }

    // toString
    @Override
    public String toString() {
        return "FilePermission{" +
                "id=" + id +
                ", file=" + file.getName() +
                ", user=" + user.getUsername() +
                ", canRead=" + canRead +
                ", canWrite=" + canWrite +
                '}';
    }
}