package com.cloudbackend.repository;

import com.cloudbackend.entity.FilePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilePermissionRepository extends JpaRepository<FilePermission, Long> {

    // Find all permissions for a specific file
    List<FilePermission> findByFileId(Long fileId);

    // Find all permissions for a specific user
    List<FilePermission> findByUserId(Long userId);

    // Find a specific permission for a file and user
    Optional<FilePermission> findByFileIdAndUserId(Long fileId, Long userId);
}