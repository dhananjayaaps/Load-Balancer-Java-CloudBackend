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

    public void saveChunk(String container, String fileName, byte[] chunkData) {
        // Define the file path
        String filePath = storageDirectory + File.separator + fileName;

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(chunkData); // Write the byte array to the file
        } catch (IOException e) {
            // Log and handle the error as needed
            System.err.println("Error saving chunk: " + e.getMessage());
        }
    }

    public byte[] getChunk(String fileName) {
        // Define the file path
        String filePath = storageDirectory + File.separator + fileName;

        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] chunkData = new byte[(int) new File(filePath).length()];
            fis.read(chunkData); // Read the file content into the byte array
            return chunkData;
        } catch (IOException e) {
            // Log and handle the error as needed
            System.err.println("Error getting chunk: " + e.getMessage());
            return null;
        }
    }
}
