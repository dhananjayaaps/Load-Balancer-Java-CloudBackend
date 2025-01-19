package com.cloudbackend.service;

import com.cloudbackend.FileManager.TrafficController;
import com.cloudbackend.FileManager.TrafficEmulator;
import org.springframework.stereotype.Service;

@Service
public class TrafficMonitoringService {
    private final TrafficController trafficController;

    public TrafficMonitoringService(TrafficController trafficController) {
        this.trafficController = trafficController;
    }

    public void monitorTrafficAndApplyDelay() throws InterruptedException {
        int activeUploads = trafficController.getActiveUploads();
        int activeDownloads = trafficController.getActiveDownloads();

        int totalActiveRequests = activeUploads + activeDownloads;
        String trafficLevel;

        if (totalActiveRequests < 5) {
            trafficLevel = "low";
        } else if (totalActiveRequests < 10) {
            trafficLevel = "medium";
        } else {
            trafficLevel = "high";
        }

        TrafficEmulator.applyTrafficEmulatedDelay(trafficLevel);
    }
}
