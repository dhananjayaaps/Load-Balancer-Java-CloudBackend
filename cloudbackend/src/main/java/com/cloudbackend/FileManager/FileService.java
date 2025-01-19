package com.cloudbackend.FileManager;

import com.cloudbackend.entity.FileMetadata;
import com.cloudbackend.entity.User;
import com.cloudbackend.repository.FileMetadataRepository;
import com.cloudbackend.util.AESUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileService {

    @Value("${STORAGE_CONTAINERS}")
    private String storageContainers; // Load the storage containers from environment variable

    @Value("${ENCRYPTION_KEY}")
    private String encryptionKey; // Load the common key from environment variable

    private final ChunkingService chunkingService;
    private final StorageClient storageClient;
    private final LoadBalancer loadBalancer;
    private final FileMetadataRepository fileMetadataRepository;

    public FileService(ChunkingService chunkingService, StorageClient storageClient, LoadBalancer loadBalancer, FileMetadataRepository fileMetadataRepository) {
        this.chunkingService = chunkingService;
        this.storageClient = storageClient;
        this.loadBalancer = loadBalancer;
        this.fileMetadataRepository = fileMetadataRepository;
    }

    public void uploadFile(String fileName, byte[] fileData, User owner, String schedulingAlgorithm, List<Integer> priorities) {
        try {
            // Split the file into chunks
            List<byte[]> chunks = chunkingService.splitFile(fileData, 1024);

            // Get the list of containers
            List<String> containers = List.of(storageContainers.split(","));

            // Save file metadata
            FileMetadata metadata = new FileMetadata(fileName, "path/to/file", (long) fileData.length, owner);
            metadata.setTotalChunks(chunks.size());
            fileMetadataRepository.save(metadata);

            // Loop through chunks and upload them to the selected containers based on the algorithm
            for (int i = 0; i < chunks.size(); i++) {
                String encryptedChunk = AESUtils.encrypt(chunks.get(i), encryptionKey);

                // Select the target container based on the scheduling algorithm
                String targetContainer = loadBalancer.handleTraffic(containers, schedulingAlgorithm, priorities);

                // Ensure encryption was successful
                assert encryptedChunk != null;

                // Save the encrypted chunk to the selected container
                storageClient.saveChunk(targetContainer, fileName + "_chunk_" + i, encryptedChunk.getBytes());
            }
        } catch (Exception e) {
            System.out.println("Error during file upload: " + e.getMessage());
            throw new RuntimeException("Error during file upload", e);
        }
    }


    private byte[] mergeChunks(byte[] existingFile, byte[] newChunk) {
        byte[] merged = new byte[existingFile.length + newChunk.length];
        System.arraycopy(existingFile, 0, merged, 0, existingFile.length);
        System.arraycopy(newChunk, 0, merged, existingFile.length, newChunk.length);
        return merged;
    }

    public byte[] downloadFile(String fileName) throws Exception {
        try {
            // Retrieve file metadata to get the number of chunks
            FileMetadata metadata = null;
            List<FileMetadata> metadataList = fileMetadataRepository.findAllByName(fileName);

            if (metadataList != null && !metadataList.isEmpty()) {
                metadata = metadataList.get(metadataList.size() - 1); // Get the last element
            }

            if (metadata == null) {
                throw new RuntimeException("File metadata not found.");
            }

            int totalChunks = metadata.getTotalChunks();
            List<String> containers = List.of(storageContainers.split(","));

            // Initialize an array to hold the merged file data
            byte[] fileData = new byte[0];

            // Download and decrypt each chunk, then merge them
            for (int i = 0; i < totalChunks; i++) {
                String targetContainer = loadBalancer.getNextContainer(containers);
                byte[] encryptedChunk = storageClient.getChunk(targetContainer, fileName + "_chunk_" + i);

                // Decrypt the chunk and ensure it is a byte array
                byte[] decryptedChunk = AESUtils.decrypt(new String(encryptedChunk), encryptionKey);

                if (decryptedChunk != null) {
                    fileData = mergeChunks(fileData, decryptedChunk);
                }
            }

            // Return the merged and decrypted file data
            return fileData;
        } catch (Exception e) {
            System.out.println("Error during file download: " + e.getMessage());
            throw new RuntimeException("Error during file download", e);
        }
    }


}
