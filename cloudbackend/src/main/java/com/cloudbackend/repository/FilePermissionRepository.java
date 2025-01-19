package com.cloudbackend.repository;

import com.cloudbackend.entity.FileMetadata;
import com.cloudbackend.entity.FilePermission;
import com.cloudbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FilePermissionRepository extends JpaRepository<FilePermission, Long> {
    Optional<FilePermission> findByFileAndRecipient(FileMetadata file, User recipient);
}
