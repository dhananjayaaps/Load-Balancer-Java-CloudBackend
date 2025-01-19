package com.cloudbackend.FileManager;

import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HealthCheck {

    private final Map<String, Boolean> containerHealthStatus = new HashMap<>();

    public void performHealthCheck(List<String> containers) {
        for (String container : containers) {
            boolean isHealthy = checkContainerHealth(container);
            containerHealthStatus.put(container, isHealthy);
        }
    }

    private boolean checkContainerHealth(String containerUrl) {
        try {
            URL url = new URL(containerUrl + "/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000); // Timeout in milliseconds
            connection.connect();

            int responseCode = connection.getResponseCode();
            return responseCode == 200; // Healthy if HTTP 200 is returned
        } catch (Exception e) {
            System.err.println("Health check failed for container: " + containerUrl);
            return false;
        }
    }

    public Map<String, Boolean> getContainerHealthStatus() {
        return containerHealthStatus;
    }
}
