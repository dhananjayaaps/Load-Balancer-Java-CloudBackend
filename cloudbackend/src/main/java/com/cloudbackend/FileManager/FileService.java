package com.cloudbackend.FileManager;

import com.cloudbackend.entity.FileMetadata;
import com.cloudbackend.entity.User;
import com.cloudbackend.repository.FileMetadataRepository;
import com.cloudbackend.service.TrafficMonitoringService;
import com.cloudbackend.util.AESUtils;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CountDownLatch;

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
    private final HealthCheck healthCheckService;

    @Autowired
    public FileService(
            ChunkingService chunkingService,
            StorageClient storageClient,
            LoadBalancer loadBalancer,
            FileMetadataRepository fileMetadataRepository,
            TrafficController trafficController,
            TrafficMonitoringService trafficMonitoringService, HealthCheck healthCheckService
    ) {
        this.chunkingService = chunkingService;
        this.storageClient = storageClient;
        this.loadBalancer = loadBalancer;
        this.fileMetadataRepository = fileMetadataRepository;
        this.trafficController = trafficController;
        this.trafficMonitoringService = trafficMonitoringService;
        this.healthCheckService = healthCheckService;
    }

    public void uploadFile(String fileName, byte[] fileData, User owner, String path, String schedulingAlgorithm, List<Integer> priorities) throws InterruptedException {
        // Determine priority and submit upload request
        int priority = calculatePriority(owner, priorities);
        trafficMonitoringService.incrementActiveRequests();

        // Check for healthy containers
        List<String> healthyContainers = checkContainerHealth();
        if (healthyContainers.isEmpty()) {
            trafficMonitoringService.decrementActiveRequests();
            throw new RuntimeException("No healthy storage containers available.");
        }

        // Use CountDownLatch to wait for task completion
        CountDownLatch latch = new CountDownLatch(1);

        trafficController.submitRequest(priority, () -> {
            try {
                processUpload(fileName, fileData, owner, path, schedulingAlgorithm, priorities);
            } catch (Exception e) {
                System.err.println("Error during upload processing: " + e.getMessage());
                throw new RuntimeException("Failed to process file upload: " + e.getMessage(), e);
            } finally {
                latch.countDown(); // Signal completion
            }
        });

        latch.await();

        trafficMonitoringService.decrementActiveRequests();
    }

    // Method to check the health of containers
    private List<String> checkContainerHealth() {
        List<String> containers = List.of(storageContainers.split(","));
        return containers.stream()
                .filter(container -> healthCheckService.checkContainerHealth(container))
                .toList();
    }



    private void processUpload(String fileName, byte[] fileData, User owner, String path, String schedulingAlgorithm, List<Integer> priorities) throws Exception {
        try {
            trafficController.acquireUploadSlot();
            
            TrafficEmulator.applyTrafficEmulatedDelay(trafficMonitoringService.determineTrafficLevel());

            List<byte[]> chunks = chunkingService.splitFile(fileData, 1024);

            List<String> containers = List.of(storageContainers.split(","));

            String savePath = "/" + owner.getName() + (path != null ? path : "");
            // Save file metadata
            FileMetadata metadata = new FileMetadata(fileName, savePath, (long) fileData.length, owner);
            metadata.setTotalChunks(chunks.size());
            fileMetadataRepository.save(metadata);

            // Encrypt and upload chunks
            for (int i = 0; i < chunks.size(); i++) {
                String encryptedChunk = AESUtils.encrypt(chunks.get(i), encryptionKey);
                String targetContainer = loadBalancer.handleTraffic(containers, schedulingAlgorithm, priorities);
                storageClient.saveChunk(targetContainer, fileName + "_chunk_" + i, encryptedChunk.getBytes());
            }
        }
        catch (Exception e) {
            System.err.println("Error during file upload: " + e.getMessage());
            throw new Exception("Failed to process file upload: " + e.getMessage(), e);
        }
        finally {
            trafficController.releaseUploadSlot();
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

    public String getFilesByOwner(Long id) {
        List<FileMetadata> files = fileMetadataRepository.findByOwner_Id(id);
        return files.toString();
    }
}
