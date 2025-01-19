package com.cloudbackend.FileManager;

public class TrafficEmulator {
    private static final int LOW_TRAFFIC_DELAY = 10000; // in ms
    private static final int MEDIUM_TRAFFIC_DELAY = 30000;
    private static final int HIGH_TRAFFIC_DELAY = 50000;

    public static void applyTrafficEmulatedDelay(String trafficLevel) throws InterruptedException {
        int delay = switch (trafficLevel.toLowerCase()) {
            case "low" -> LOW_TRAFFIC_DELAY;
            case "medium" -> MEDIUM_TRAFFIC_DELAY;
            case "high" -> HIGH_TRAFFIC_DELAY;
            default -> 0;
        };
        delay += (int) (Math.random() * 1000); // Random spike
        Thread.sleep(delay);
    }
}
