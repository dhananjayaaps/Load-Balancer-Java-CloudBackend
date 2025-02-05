package com.cloudbackend.FileManager;

import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;

@Component
public class StorageClient {

    private final String storageDirectory;

    public StorageClient() {
        // Set the base directory for storing chunks
        this.storageDirectory = "./fileStorage"; // Local directory for testing
        File dir = new File(storageDirectory);
        if (!dir.exists()) {
            dir.mkdirs(); // Create directory if it doesn't exist
        }
    }

    private String sanitizeContainerName(String container) {
        // Replace any invalid characters in the container name with underscores
        return container.replaceAll("[^a-zA-Z0-9]", "_");
    }

    public void saveChunk(String container, String fileName, byte[] chunkData) {
        // Sanitize the container name to ensure it's valid for use in a file path
        String sanitizedContainer = sanitizeContainerName(container);

        // Define the container directory
        String containerDir = storageDirectory + File.separator + sanitizedContainer;
        File dir = new File(containerDir);
        if (!dir.exists()) {
            dir.mkdirs(); // Create container directory if it doesn't exist
        }

        // Define the file path to save the chunk
        String filePath = containerDir + File.separator + fileName;

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(chunkData); // Write the byte array to the file
        } catch (IOException e) {
            // Log and handle the error as needed
            System.err.println("Error saving chunk: " + e.getMessage());
        }
    }

    public byte[] getChunk(String container, String fileName) {
        // Sanitize the container name to ensure it's valid for use in a file path
        String sanitizedContainer = sanitizeContainerName(container);

        // Construct the path to the file chunk in the container
        String filePath = storageDirectory + File.separator + sanitizedContainer + File.separator + fileName;

        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] chunkData = new byte[(int) new File(filePath).length()];
            fis.read(chunkData);  // Read the file content into the byte array
            return chunkData;
        } catch (IOException e) {
            // Log and handle the error as needed
            System.err.println("Error getting chunk: " + e.getMessage());
            return null;
        }
    }

    public void deleteChunk(String container, String fileName) {
        String sanitizedContainer = sanitizeContainerName(container);
        String filePath = storageDirectory + File.separator + sanitizedContainer + File.separator + fileName;

        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

}
