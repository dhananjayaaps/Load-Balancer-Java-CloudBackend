package com.cloudbackend.FileManager;

import org.springframework.stereotype.Component;
import java.util.concurrent.Semaphore;

@Component
public class TrafficController {
    private final Semaphore uploadSemaphore;
    private final Semaphore downloadSemaphore;

    public TrafficController() {
        // Define maximum concurrent uploads and downloads
        int maxConcurrentUploads = 20;
        int maxConcurrentDownloads = 20;
        this.uploadSemaphore = new Semaphore(maxConcurrentUploads);
        this.downloadSemaphore = new Semaphore(maxConcurrentDownloads);
    }

    public void acquireUploadSlot() throws InterruptedException {
        uploadSemaphore.acquire();
    }

    public void releaseUploadSlot() {
        uploadSemaphore.release();
    }

    public void acquireDownloadSlot() throws InterruptedException {
        downloadSemaphore.acquire();
    }

    public void releaseDownloadSlot() {
        downloadSemaphore.release();
    }

    public int getActiveUploads() {
        return uploadSemaphore.availablePermits();
    }

    public int getActiveDownloads() {
        return downloadSemaphore.availablePermits();
    }
}
