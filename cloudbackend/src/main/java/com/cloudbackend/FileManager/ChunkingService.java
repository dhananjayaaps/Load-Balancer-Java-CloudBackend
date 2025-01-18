package com.cloudbackend.FileManager;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    public List<byte[]> splitFile(byte[] fileData, int chunkSize) {
        List<byte[]> chunks = new ArrayList<>();
        int start = 0;
        while (start < fileData.length) {
            int end = Math.min(start + chunkSize, fileData.length);
            byte[] chunk = new byte[end - start];
            System.arraycopy(fileData, start, chunk, 0, end - start);
            chunks.add(chunk);
            start = end;
        }
        return chunks;
    }
}
