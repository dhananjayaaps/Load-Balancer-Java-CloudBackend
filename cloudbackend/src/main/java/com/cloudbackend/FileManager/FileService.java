package com.cloudbackend.FileManager;

import com.cloudbackend.dto.FileDTO;
import com.cloudbackend.entity.FileMetadata;
import com.cloudbackend.entity.FilePermission;
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
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

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

    public void uploadFile(String fileNameOriginal, byte[] fileData, User owner, String path,
                           String schedulingAlgorithm, List<Integer> priorities,
                           boolean othersCanRead, boolean othersCanWrite) throws InterruptedException {

        // Determine priority and submit upload request
        int priority = calculatePriority(owner, priorities);
        trafficMonitoringService.incrementActiveRequests();

        // Check for healthy containers
        List<String> healthyContainers = checkContainerHealth();
        if (healthyContainers.isEmpty()) {
            trafficMonitoringService.decrementActiveRequests();
            throw new RuntimeException("No healthy storage containers available.");
        }

//        make a random value to add to the file name
        String randomValue = String.valueOf((int) (Math.random() * 1000));

        String fileName = owner.getUsername() + "_" + randomValue + "_"+ fileNameOriginal;

        // Use CountDownLatch to wait for task completion
        CountDownLatch latch = new CountDownLatch(1);

        trafficController.submitRequest(priority, () -> {
            try {
                processUpload(fileNameOriginal, fileName, fileData, owner, path, schedulingAlgorithm, priorities);
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



    private void processUpload(String fileNameOriginal, String fileName, byte[] fileData, User owner, String path, String schedulingAlgorithm, List<Integer> priorities) throws Exception {
        try {
            trafficController.acquireUploadSlot();

            TrafficEmulator.applyTrafficEmulatedDelay(trafficMonitoringService.determineTrafficLevel());

            List<byte[]> chunks = chunkingService.splitFile(fileData, 1024);

            List<String> containers = List.of(storageContainers.split(","));

            String savePath = "/" + owner.getUsername() + path + "/" + fileNameOriginal;
            // Normalize the path to avoid duplicates or invalid formats
            savePath = savePath.replaceAll("/+", "/"); // Replace multiple slashes with one
            savePath = savePath.replaceAll("/$", ""); // Remove trailing slash

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

    public byte[] downloadFile(String filePath, User requester) throws Exception{
        try {
            FileMetadata metadata = fileMetadataRepository.findByPath(filePath)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            if(!hasReadAccess(metadata, requester)) {
                throw new SecurityException("No read permission");
            }
            trafficController.acquireDownloadSlot();

            int totalChunks = metadata.getTotalChunks();
            List<String> containers = List.of(storageContainers.split(","));

            byte[] fileData = new byte[0];

            // Use metadata.getName() to construct chunk names
            for (int i = 0; i < totalChunks; i++) {
                String targetContainer = loadBalancer.getNextContainer(containers);
                String chunkName = metadata.getName() + "_chunk_" + i; // Use the generated filename
                byte[] encryptedChunk = storageClient.getChunk(targetContainer, chunkName);

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

    public List<String> getFilePathsByOwner(Long ownerId) {
        List<FileMetadata> files = fileMetadataRepository.findByOwner_Id(ownerId);
        // Extract the file paths from the metadata
        return files.stream()
                .map(FileMetadata::getPath)
                .collect(Collectors.toList());
    }

    public List<FileDTO> getFilesWithPermissions(User user) {
        List<FileMetadata> files = fileMetadataRepository.findByOwner_Id(user.getId());
        return files.stream().map(file ->
                new FileDTO(
                        file.getPath(),
                        file.getSize(),
                        true,  // owner always has access
                        true,
                        file.isOthersCanRead(),
                        file.isOthersCanWrite()
                )
        ).collect(Collectors.toList());
    }

    public void updateOthersPermissions(Long fileId, boolean canRead, boolean canWrite) {
        FileMetadata file = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        file.setOthersCanRead(canRead);
        file.setOthersCanWrite(canWrite);
        fileMetadataRepository.save(file);
    }

    private boolean hasReadAccess(FileMetadata file, User user) {
        if(file.getOwner().equals(user)) return true;

        // Check specific permissions
        Optional<FilePermission> permission = file.getPermissions().stream()
                .filter(p -> p.getUser().equals(user))
                .findFirst();

        if(permission.isPresent()) return permission.get().isCanRead();

        // Check others permissions
        return file.isOthersCanRead();
    }
}
