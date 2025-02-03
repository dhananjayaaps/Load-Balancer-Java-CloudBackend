package com.cloudbackend.repository;

import com.cloudbackend.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findByOwner_Id(Long ownerId);
//    Optional<FileMetadata> findByFileName(String fileName);
    FileMetadata findByName(String name);

    List<FileMetadata> findAllByName(String fileName);
    Optional<FileMetadata> findByPath(String path);
    List<FileMetadata> findByPathStartingWith(String path);
    boolean existsByPath(String path);
}
