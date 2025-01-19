package com.cloudbackend.FileManager;

import com.cloudbackend.entity.FileMetadata;
import com.cloudbackend.entity.User;
import com.cloudbackend.repository.FileMetadataRepository;
import com.cloudbackend.service.TrafficMonitoringService;
import com.cloudbackend.util.AESUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileService {

    @Value("${STORAGE_CONTAINERS}")
    private String storageContainers; // Storage containers from environment variable

    @Value("${ENCRYPTION_KEY}")
    private String encryptionKey; // Encryption key from environment variable

    private final ChunkingService chunkingService;
    private final StorageClient storageClient;
    private final LoadBalancer loadBalancer;
    private final FileMetadataRepository fileMetadataRepository;
    private final TrafficController trafficController;
    private final TrafficMonitoringService trafficMonitoringService;

    @Autowired
    public FileService(
            ChunkingService chunkingService,
            StorageClient storageClient,
            LoadBalancer loadBalancer,
            FileMetadataRepository fileMetadataRepository,
            TrafficController trafficController,
            TrafficMonitoringService trafficMonitoringService
    ) {
        this.chunkingService = chunkingService;
        this.storageClient = storageClient;
        this.loadBalancer = loadBalancer;
        this.fileMetadataRepository = fileMetadataRepository;
        this.trafficController = trafficController;
        this.trafficMonitoringService = trafficMonitoringService;
    }

    public void uploadFile(String fileName, byte[] fileData, User owner, String schedulingAlgorithm, List<Integer> priorities) {
        try {
            // Determine priority and submit upload request
            int priority = calculatePriority(owner, priorities);
            trafficMonitoringService.incrementActiveRequests();
            trafficController.submitRequest(priority, () -> {
                try {

                    processUpload(fileName, fileData, owner, schedulingAlgorithm, priorities);
                } catch (Exception e) {
                    System.err.println("Error during upload processing: " + e.getMessage());
                }
//                trafficMonitoringService.incrementActiveRequests();
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request submission interrupted", e);
        } finally {
            trafficMonitoringService.decrementActiveRequests();
        }
    }

    private void processUpload(String fileName, byte[] fileData, User owner, String schedulingAlgorithm, List<Integer> priorities) throws Exception {

        TrafficEmulator.applyTrafficEmulatedDelay(trafficMonitoringService.determineTrafficLevel());

        List<byte[]> chunks = chunkingService.splitFile(fileData, 1024);

        List<String> containers = List.of(storageContainers.split(","));

        // Save file metadata
        FileMetadata metadata = new FileMetadata(fileName, "path/to/file", (long) fileData.length, owner);
        metadata.setTotalChunks(chunks.size());
        fileMetadataRepository.save(metadata);

        // Encrypt and upload chunks
        for (int i = 0; i < chunks.size(); i++) {
            String encryptedChunk = AESUtils.encrypt(chunks.get(i), encryptionKey);
            String targetContainer = loadBalancer.handleTraffic(containers, schedulingAlgorithm, priorities);
            storageClient.saveChunk(targetContainer, fileName + "_chunk_" + i, encryptedChunk.getBytes());
        }
    }

    private int calculatePriority(User owner, List<Integer> priorities) {
        // Calculate priority based on provided logic
        return priorities != null && !priorities.isEmpty() ? priorities.get(0) : 0;
    }

    private byte[] mergeChunks(byte[] existingFile, byte[] newChunk) {
        byte[] merged = new byte[existingFile.length + newChunk.length];
        System.arraycopy(existingFile, 0, merged, 0, existingFile.length);
        System.arraycopy(newChunk, 0, merged, existingFile.length, newChunk.length);
        return merged;
    }

    public byte[] downloadFile(String fileName) throws Exception {
        try {
            // Retrieve metadata for the file
            FileMetadata metadata = fileMetadataRepository.findAllByName(fileName)
                    .stream()
                    .reduce((first, second) -> second) // Get the last metadata entry
                    .orElseThrow(() -> new RuntimeException("File metadata not found."));

            trafficController.acquireDownloadSlot();

            int totalChunks = metadata.getTotalChunks();
            List<String> containers = List.of(storageContainers.split(","));

            // Initialize byte array for the merged file
            byte[] fileData = new byte[0];

            // Download and decrypt each chunk
            for (int i = 0; i < totalChunks; i++) {
                String targetContainer = loadBalancer.getNextContainer(containers);
                byte[] encryptedChunk = storageClient.getChunk(targetContainer, fileName + "_chunk_" + i);

                // Decrypt and merge the chunk
                byte[] decryptedChunk = AESUtils.decrypt(new String(encryptedChunk), encryptionKey);
                if (decryptedChunk != null) {
                    fileData = mergeChunks(fileData, decryptedChunk);
                }
            }

            return fileData;
        } catch (Exception e) {
            System.err.println("Error during file download: " + e.getMessage());
            throw new RuntimeException("Error during file download", e);
        }
    }
}
