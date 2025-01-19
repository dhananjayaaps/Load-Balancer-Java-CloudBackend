package com.cloudbackend.FileManager;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoadBalancer {

    private final AtomicInteger counter = new AtomicInteger(0);

    // Example for load balancing algorithms:
    private final Map<String, Integer> serverLoad = new HashMap<>(); // Store the load for each server

    public LoadBalancer() {
        // Initialize server load with zero for each container (you can adjust it dynamically)
    }

    // Round Robin load balancing
    public String getNextContainer(List<String> containers) {
        int index = counter.getAndIncrement() % containers.size();
        return containers.get(index);
    }

    // Priority Scheduling (tasks with higher priority should be processed first)
    public String getContainerWithPriority(List<String> containers, List<Integer> priorities) {
        // Assume priorities is a list where index corresponds to a container
        int highestPriorityIndex = 0;
        for (int i = 1; i < priorities.size(); i++) {
            if (priorities.get(i) > priorities.get(highestPriorityIndex)) {
                highestPriorityIndex = i;
            }
        }
        return containers.get(highestPriorityIndex);
    }

    // Shortest Job Next (SJN) - selecting containers with the least load
    public String getContainerWithShortestJob(List<String> containers) {
        String selectedContainer = containers.get(0);
        int minLoad = serverLoad.getOrDefault(selectedContainer, 0);

        for (String container : containers) {
            int load = serverLoad.getOrDefault(container, 0);
            if (load < minLoad) {
                minLoad = load;
                selectedContainer = container;
            }
        }

        return selectedContainer;
    }

    // Update the load of a container (this can be based on task completion or other criteria)
    public void updateContainerLoad(String container, int loadChange) {
        serverLoad.put(container, serverLoad.getOrDefault(container, 0) + loadChange);
    }

    // Method to handle traffic spikes and optimize load balancing
    public String handleTraffic(List<String> containers, String algorithm, List<Integer> priorities) {
        switch (algorithm) {
            case "RR":
                return getNextContainer(containers); // Round Robin
            case "Priority":
                return getContainerWithPriority(containers, priorities); // Priority Scheduling
            case "SJN":
                return getContainerWithShortestJob(containers); // Shortest Job Next
            default:
                return getNextContainer(containers); // Default to Round Robin
        }
    }
}
