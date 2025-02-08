package com.cloudbackend.FileManager;

import com.cloudbackend.dto.FileDTO;
import com.cloudbackend.entity.FileMetadata;
import com.cloudbackend.entity.User;
import com.cloudbackend.exception.PermissionDeniedException;
import com.cloudbackend.exception.ResourceNotFoundException;
import com.cloudbackend.repository.FileMetadataRepository;
import com.cloudbackend.service.TrafficMonitoringService;
import com.cloudbackend.util.AESUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

        System.out.println("Uploading file: " + fileNameOriginal);

        // Determine priority and submit upload request
        int priority = calculatePriority(owner, priorities);
        trafficMonitoringService.incrementActiveRequests();

        // Check for healthy containers
        List<String> healthyContainers = checkContainerHealth();
        if (healthyContainers.isEmpty()) {
            trafficMonitoringService.decrementActiveRequests();
            throw new RuntimeException("No healthy storage containers available.");
        }

        String fileName = owner.getUsername() + "_" + fileNameOriginal;
        String randomValue = String.valueOf((int) (Math.random() * 1000));
        fileName = fileName + "_" + randomValue;

        // Check if file already exists
        String savePath = path;
        savePath = savePath.replaceAll("/+", "/").replaceAll("/$", ""); // Normalize path

        Optional<FileMetadata> existingFileOpt = fileMetadataRepository.findByPath(savePath);

        // Delete old chunks
        existingFileOpt.ifPresent(this::deleteChunks);

        CountDownLatch latch = new CountDownLatch(1);

        String finalFileName = fileName;
        trafficController.submitRequest(priority, () -> {
            try {
                processUpload(fileNameOriginal, finalFileName, fileData, owner, path, schedulingAlgorithm, priorities, existingFileOpt);
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



    private void processUpload(String fileNameOriginal, String fileName, byte[] fileData, User owner, String path,
                               String schedulingAlgorithm, List<Integer> priorities, Optional<FileMetadata> existingFileOpt) throws Exception {
        try {
            trafficController.acquireUploadSlot();
            TrafficEmulator.applyTrafficEmulatedDelay(trafficMonitoringService.determineTrafficLevel());

            List<byte[]> chunks = chunkingService.splitFile(fileData, 1024);
            List<String> containers = List.of(storageContainers.split(","));

            String savePath = "/" + owner.getUsername() + path + "/" + fileNameOriginal;
            savePath = savePath.replaceAll("/+", "/").replaceAll("/$", "");

            FileMetadata metadata = existingFileOpt.orElse(new FileMetadata(fileName, savePath, (long) fileData.length, owner));

            metadata.setSize((long) fileData.length);
            metadata.setTotalChunks(chunks.size());
            fileMetadataRepository.save(metadata);

            // Encrypt and upload chunks
            for (int i = 0; i < chunks.size(); i++) {
                String encryptedChunk = AESUtils.encrypt(chunks.get(i), encryptionKey);
                String targetContainer = loadBalancer.handleTraffic(containers, schedulingAlgorithm, priorities);
                storageClient.saveChunk(targetContainer, fileName + "_chunk_" + i, encryptedChunk.getBytes());
            }
        } catch (Exception e) {
            System.err.println("Error during file upload: " + e.getMessage());
            throw new Exception("Failed to process file upload: " + e.getMessage(), e);
        } finally {
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

    public byte[] downloadFile(String filePath, User requester, boolean autoPermission) throws Exception {
        try {
            FileMetadata metadata = fileMetadataRepository.findByPath(filePath)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            if (autoPermission) {
                System.out.println("Copy is processing");
            } else if (!hasReadAccess(metadata, requester)) {
                throw new SecurityException("No read permission");
            }

            trafficController.acquireDownloadSlot();

            int totalChunks = metadata.getTotalChunks();
            List<String> containers = List.of(storageContainers.split(","));
            byte[] fileData = new byte[0];

            for (int i = 0; i < totalChunks; i++) {
                String chunkName = metadata.getName() + "_chunk_" + i;
                byte[] encryptedChunk = null;

                // Try fetching the chunk from available containers
                for (String container : containers) {
                    encryptedChunk = storageClient.getChunk(container, chunkName);
                    if (encryptedChunk != null) {
                        break; // Found the chunk, move to decryption
                    }
                }

                if (encryptedChunk == null) {
                    System.err.println("Chunk missing: " + chunkName + ". Skipping...");
                    continue; // Skip this chunk if not found in any container
                }

                // Decrypt and merge
                byte[] decryptedChunk = AESUtils.decrypt(new String(encryptedChunk), encryptionKey);
                if (decryptedChunk != null) {
                    fileData = mergeChunks(fileData, decryptedChunk);
                } else {
                    System.err.println("Failed to decrypt chunk: " + chunkName);
                }
            }

            return fileData;
        } catch (Exception e) {
            System.err.println("Error during file download: " + e.getMessage());
            throw new RuntimeException("Error during file download", e);
        }
    }


    public List<String> getFilePathsByOwner(Long ownerId) {
        List<FileMetadata> files = fileMetadataRepository.findByOwner_Id(ownerId);
        // Extract the file paths from the metadata
        return files.stream()
                .map(FileMetadata::getPath)
                .collect(Collectors.toList());
    }

    public void updateOthersPermissions(Long fileId, boolean canRead, boolean canWrite) {
        FileMetadata file = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        file.setOthersCanRead(canRead);
        file.setOthersCanWrite(canWrite);
        fileMetadataRepository.save(file);
    }


    public List<FileDTO> listFiles(String path, User user) {
        List<FileMetadata> files = fileMetadataRepository.findByPathStartingWith(path);
        return files.stream()
                .filter(file -> hasReadAccess(file, user))
                .map(file -> new FileDTO(
                        file.getPath(),
                        file.getSize(),
                        hasReadAccess(file, user),
                        hasWriteAccess(file, user),
                        file.isOthersCanRead(),
                        file.isOthersCanWrite(),
                        file.isDirectory()
                ))
                .collect(Collectors.toList());
    }

    public void createFile(String path, String fileName, User user) {
        String fullPath = path + "/" + fileName;
        if (fileMetadataRepository.existsByPath(fullPath)) {
            throw new IllegalArgumentException("File already exists");
        }

        String randomValue = String.valueOf((int) (Math.random() * 1000));
        fileName = fileName + "_" + randomValue;

        FileMetadata metadata = new FileMetadata(
                fileName,
                fullPath,
                0L, // Initial size
                user,
                false, // othersCanRead
                false, // othersCanWrite
                false  // isDirectory
        );
        fileMetadataRepository.save(metadata);
    }

    public void createDirectory(String path, String dirName, User user) {
        String fullPath = path + "/" + dirName;
        if (fileMetadataRepository.existsByPath(fullPath)) {
            throw new IllegalArgumentException("Directory already exists");
        }

        FileMetadata metadata = new FileMetadata(
                dirName,
                fullPath,
                0L, // Directory size
                user,
                false, // othersCanRead
                false, // othersCanWrite
                true   // isDirectory
        );
        fileMetadataRepository.save(metadata);
    }

    public void deleteFileOrDirectory(String path, User user) {
        FileMetadata metadata = fileMetadataRepository.findByPath(path)
                .orElseThrow(() -> new ResourceNotFoundException("File or directory not found"));

        if (!hasWriteAccess(metadata, user)) {
            throw new PermissionDeniedException("You do not have permission to delete this file/directory");
        }

        // If it's a directory, recursively delete its contents
        if (metadata.isDirectory()) {
            deleteDirectoryContents(path, user);
        }

        // Delete the file chunks from storage containers
        deleteChunks(metadata);

        // Finally, delete the metadata entry from the database
        fileMetadataRepository.delete(metadata);
    }

    private void deleteDirectoryContents(String directoryPath, User user) {
        // Find all files and subdirectories within the directory
        List<FileMetadata> contents = fileMetadataRepository.findByPathStartingWith(directoryPath + "/");

        for (FileMetadata content : contents) {
            // Recursively delete each file or subdirectory
            deleteFileOrDirectory(content.getPath(), user);
        }
    }

    private boolean hasReadAccess(FileMetadata file, User user) {
        return file.getOwner().equals(user) ||
                file.getPermissions().stream()
                        .anyMatch(p -> p.getUser().equals(user) && p.isCanRead()) ||
                file.isOthersCanRead();
    }

    private boolean hasWriteAccess(FileMetadata file, User user) {
        return file.getOwner().equals(user) ||
                file.getPermissions().stream()
                        .anyMatch(p -> p.getUser().equals(user) && p.isCanWrite()) ||
                file.isOthersCanWrite();
    }

    private void deleteChunks(FileMetadata fileMetadata) {
        List<String> containers = List.of(storageContainers.split(","));

        for (int i = 0; i < fileMetadata.getTotalChunks(); i++) {
            String chunkName = fileMetadata.getName() + "_chunk_" + i;
            boolean deleted = false;

            for (String container : containers) {
                if (storageClient.deleteChunk(container, chunkName)) { // Ensure deleteChunk returns success
                    deleted = true;
                    break; // Stop searching once deleted
                }
            }

            if (!deleted) {
                System.err.println("Chunk not found for deletion: " + chunkName);
            }
        }
    }


    public void updateFile(String fileNameOriginal, byte[] fileData, User owner, String path,
                           String schedulingAlgorithm, List<Integer> priorities,
                           boolean othersCanRead, boolean othersCanWrite) throws InterruptedException {

        System.out.println("Uploading file: " + fileNameOriginal);

        // Determine priority and submit upload request
        int priority = calculatePriority(owner, priorities);
        trafficMonitoringService.incrementActiveRequests();

        // Check for healthy containers
        List<String> healthyContainers = checkContainerHealth();
        if (healthyContainers.isEmpty()) {
            trafficMonitoringService.decrementActiveRequests();
            throw new RuntimeException("No healthy storage containers available.");
        }

        FileMetadata file = fileMetadataRepository.findByPath(path)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        String savePath = file.getPath();
        savePath = savePath.replaceAll("/+", "/").replaceAll("/$", ""); // Normalize path

        Optional<FileMetadata> existingFileOpt = fileMetadataRepository.findByPath(savePath);

        existingFileOpt.ifPresent(this::deleteChunks);

        CountDownLatch latch = new CountDownLatch(1);

        trafficController.submitRequest(priority, () -> {
            try {
                processUpload(fileNameOriginal, file.getName(), fileData, owner, path, schedulingAlgorithm, priorities, existingFileOpt);
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

    public void updateOthersPermissions(String path, boolean canRead, boolean canWrite, User requester) {
        System.out.println(path + " " + canRead + " " + canWrite);
        FileMetadata file = fileMetadataRepository.findByPath(path)
                .orElseThrow(() -> new ResourceNotFoundException("File or directory not found"));

        // Check if the requester is the owner of the file/directory
        if (!file.getOwner().equals(requester)) {
            throw new PermissionDeniedException("Only the owner can update permissions for this file/directory.");
        }

        // Update the permissions
        file.setOthersCanRead(canRead);
        file.setOthersCanWrite(canWrite);

        // If it's a directory, update permissions for all its contents recursively
        if (file.isDirectory()) {
            updatePermissionsForDirectoryContents(file.getPath(), canRead, canWrite);
        }

        // Save the updated metadata
        fileMetadataRepository.save(file);
    }

    private void updatePermissionsForDirectoryContents(String directoryPath, boolean canRead, boolean canWrite) {
        // Find all files and subdirectories within the directory
        List<FileMetadata> contents = fileMetadataRepository.findByPathStartingWith(directoryPath + "/");

        // Update permissions for each file or subdirectory
        for (FileMetadata content : contents) {
            content.setOthersCanRead(canRead);
            content.setOthersCanWrite(canWrite);
            fileMetadataRepository.save(content);
        }
    }

    public List<FileDTO> buildTree(String path, User user) {
        List<FileMetadata> files = fileMetadataRepository.findByPathStartingWith(path);
        return files.stream()
                .map(file -> new FileDTO(
                        file.getPath(),
                        file.getSize(),
                        false,
                        false,
                        file.isOthersCanRead(),
                        file.isOthersCanWrite(),
                        file.isDirectory()
                ))
                .collect(Collectors.toList());
    }


}
