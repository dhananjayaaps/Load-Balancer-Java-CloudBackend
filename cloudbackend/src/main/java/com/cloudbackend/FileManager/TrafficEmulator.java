package com.cloudbackend.FileManager;

public class TrafficEmulator {
    private static final int LOW_TRAFFIC_DELAY = 30000; // in milliseconds
    private static final int MEDIUM_TRAFFIC_DELAY = 60000;
    private static final int HIGH_TRAFFIC_DELAY = 90000;

    public static void applyTrafficEmulatedDelay(String trafficLevel) throws InterruptedException {
        int delay;
        switch (trafficLevel.toLowerCase()) {
            case "low":
                delay = LOW_TRAFFIC_DELAY;
                break;
            case "medium":
                delay = MEDIUM_TRAFFIC_DELAY;
                break;
            case "high":
                delay = HIGH_TRAFFIC_DELAY;
                break;
            default:
                delay = 0;
        }
        Thread.sleep(delay);
    }
}
