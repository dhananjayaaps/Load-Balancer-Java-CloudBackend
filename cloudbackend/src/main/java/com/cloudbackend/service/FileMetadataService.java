package com.cloudbackend.service;

import com.cloudbackend.entity.FileMetadata;
import com.cloudbackend.entity.User;
import com.cloudbackend.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FileMetadataService {

    private final FileMetadataRepository fileMetadataRepository;

    public FileMetadataService(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
    }

    public FileMetadata saveFileMetadata(String name, String path, Long size, User owner) {
        FileMetadata fileMetadata = new FileMetadata(name, path, size, owner);
        return fileMetadataRepository.save(fileMetadata);
    }

    public Optional<FileMetadata> getFileById(Long id) {
        return fileMetadataRepository.findById(id);
    }

    public List<FileMetadata> getFilesByOwner(Long ownerId) {
        return fileMetadataRepository.findByOwner_Id(ownerId);
    }

    public void deleteFile(Long fileId) {
        fileMetadataRepository.deleteById(fileId);
    }
}
