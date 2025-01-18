package com.cloudbackend.FileManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileService {

    @Value("${STORAGE_CONTAINERS}")
    private String storageContainers;

    private final ChunkingService chunkingService;
    private final StorageClient storageClient;

    public FileService(ChunkingService chunkingService, StorageClient storageClient) {
        this.chunkingService = chunkingService;
        this.storageClient = storageClient;
    }

    public void uploadFile(String fileName, byte[] fileData) {
        List<byte[]> chunks = chunkingService.splitFile(fileData, 1024); // 1 MB chunks
        String[] containers = storageContainers.split(",");
        for (int i = 0; i < chunks.size(); i++) {
            String targetContainer = containers[i % containers.length];
            storageClient.saveChunk(targetContainer, fileName + "_chunk_" + i, chunks.get(i));
        }
    }

    public byte[] downloadFile(String fileName, int totalChunks) {
        byte[] completeFile = new byte[0];
        String[] containers = storageContainers.split(",");
        for (int i = 0; i < totalChunks; i++) {
            String sourceContainer = containers[i % containers.length];
            byte[] chunk = storageClient.getChunk(sourceContainer, fileName + "_chunk_" + i);
            completeFile = mergeChunks(completeFile, chunk);
        }
        return completeFile;
    }

    private byte[] mergeChunks(byte[] existingFile, byte[] newChunk) {
        byte[] merged = new byte[existingFile.length + newChunk.length];
        System.arraycopy(existingFile, 0, merged, 0, existingFile.length);
        System.arraycopy(newChunk, 0, merged, existingFile.length, newChunk.length);
        return merged;
    }
}
