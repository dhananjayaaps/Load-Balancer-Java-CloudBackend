package com.cloudbackend.FileManager;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class StorageClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public void saveChunk(String containerUrl, String fileName, byte[] chunkData) {
        restTemplate.postForObject(containerUrl + "/upload", chunkData, Void.class);
    }

    public byte[] getChunk(String containerUrl, String fileName) {
        return restTemplate.getForObject(containerUrl + "/download?file=" + fileName, byte[].class);
    }
}
