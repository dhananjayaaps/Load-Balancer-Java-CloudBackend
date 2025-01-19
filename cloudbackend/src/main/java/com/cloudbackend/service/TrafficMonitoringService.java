package com.cloudbackend.service;

import com.cloudbackend.FileManager.TrafficEmulator;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TrafficMonitoringService {
    private final AtomicInteger activeRequests;
    private int lowTrafficThreshold;
    private int mediumTrafficThreshold;
    private int highTrafficThreshold;

    public TrafficMonitoringService() {
        this.activeRequests = new AtomicInteger(0);
        this.lowTrafficThreshold = 10; // Example threshold for low traffic
        this.mediumTrafficThreshold = 50; // Example threshold for medium traffic
        this.highTrafficThreshold = 100; // Example threshold for high traffic
    }

    public void incrementActiveRequests() {
        activeRequests.incrementAndGet();
    }

    public void decrementActiveRequests() {
        activeRequests.decrementAndGet();
    }

    public String determineTrafficLevel() {
        int currentRequests = activeRequests.get();

        if (currentRequests <= lowTrafficThreshold) {
            return "low";
        } else if (currentRequests <= mediumTrafficThreshold) {
            return "medium";
        } else {
            return "high";
        }
    }

    public void monitorTrafficAndApplyDelay() {
        String trafficLevel = determineTrafficLevel();
        try {
            System.out.println("Current traffic level: " + trafficLevel);
            TrafficEmulator.applyTrafficEmulatedDelay(trafficLevel);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Traffic delay interrupted: " + e.getMessage());
        }
    }

    public synchronized void adjustThresholds(int low, int medium, int high) {
        if (low < medium && medium < high) {
            System.out.println("Adjusting traffic thresholds: Low=" + low + ", Medium=" + medium + ", High=" + high);
            this.lowTrafficThreshold = low;
            this.mediumTrafficThreshold = medium;
            this.highTrafficThreshold = high;
        } else {
            throw new IllegalArgumentException("Invalid thresholds. Ensure Low < Medium < High.");
        }
    }

    public void logTrafficDetails() {
        System.out.println("Active requests: " + activeRequests.get());
        System.out.println("Traffic thresholds: Low=" + lowTrafficThreshold +
                ", Medium=" + mediumTrafficThreshold +
                ", High=" + highTrafficThreshold);
    }
}
