package com.cloudbackend.FileManager;

import com.cloudbackend.entity.FileMetadata;
import com.cloudbackend.entity.User;
import com.cloudbackend.repository.FileMetadataRepository;
import com.cloudbackend.util.AESUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public class FileService {

    @Value("${STORAGE_CONTAINERS}")
    private String storageContainers; // Load the storage containers from environment variable

    @Value("${ENCRYPTION_KEY}")
    private String encryptionKey; // Load the common key from environment variable

    private final ChunkingService chunkingService;
    private final StorageClient storageClient;
    private final LoadBalancer loadBalancer = new LoadBalancer();
    private final FileMetadataRepository fileMetadataRepository;

    public FileService(ChunkingService chunkingService, StorageClient storageClient, FileMetadataRepository fileMetadataRepository) {
        this.chunkingService = chunkingService;
        this.storageClient = storageClient;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    public void uploadFile(String fileName, byte[] fileData, User owner) {
        try {
            List<byte[]> chunks = chunkingService.splitFile(fileData, 1024);
            List<String> containers = List.of(storageContainers.split(","));

            // Save file metadata
            FileMetadata metadata = new FileMetadata(fileName, "path/to/file", (long) fileData.length, owner);
            metadata.setTotalChunks(chunks.size());
            fileMetadataRepository.save(metadata);

//            System.out.println("Uploading file: " + fileName + " to " + containers.size() + " containers");

            // Encrypt and upload each chunk
            for (int i = 0; i < chunks.size(); i++) {
//                String encryptedChunk = AESUtils.encrypt(chunks.get(i), encryptionKey);
                String targetContainer = loadBalancer.getNextContainer(containers);
                storageClient.saveChunk(targetContainer, fileName + "_chunk_" + i, chunks.get(i));
            }
        } catch (Exception e) {
            System.out.println("Error during file upload: " + e.getMessage());
            throw new RuntimeException("Error during file upload", e);
        }
    }

//    @GetMapping("/download")
//    public ResponseEntity<Resource> downloadFile(
//            @RequestParam String fileName,
//            @RequestHeader("Authorization") String bearerToken) {
//
//        try {
//            // Extract user from the token
//            String username = jwtUtils.extractUsernameFromToken(bearerToken.substring(7));
//            User user = userRepository.findByUsername(username)
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//
//            // Check if the user is the owner of the file
//            FileMetadata metadata = fileMetadataRepository.findByName(fileName)
//                    .orElseThrow(() -> new RuntimeException("File not found"));
//            if (!metadata.getOwner().equals(user)) {
//                throw new AccessDeniedException("You do not have permission to access this file");
//            }
//
//            // Download the file
//            byte[] fileData = fileService.downloadFile(fileName);
//            ByteArrayResource resource = new ByteArrayResource(fileData);
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
//                    .body((Resource) resource);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(null);
//        }
//    }


    private byte[] mergeChunks(byte[] existingFile, byte[] newChunk) {
        byte[] merged = new byte[existingFile.length + newChunk.length];
        System.arraycopy(existingFile, 0, merged, 0, existingFile.length);
        System.arraycopy(newChunk, 0, merged, existingFile.length, newChunk.length);
        return merged;
    }
}
