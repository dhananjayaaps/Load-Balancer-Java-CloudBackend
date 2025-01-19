package com.cloudbackend.FileManager;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

@Component
public class TrafficController {
    private final Semaphore uploadSemaphore;
    private final Semaphore downloadSemaphore;

    // Priority queue to manage requests with aging
    private final PriorityQueue<PriorityTask> taskQueue;
    private final Thread taskProcessor;

    public TrafficController() {
        // Define maximum concurrent uploads and downloads
        int maxConcurrentUploads = 20;
        int maxConcurrentDownloads = 20;
        this.uploadSemaphore = new Semaphore(maxConcurrentUploads);
        this.downloadSemaphore = new Semaphore(maxConcurrentDownloads);

        // Initialize priority queue for tasks
        this.taskQueue = new PriorityQueue<>(Comparator.comparingInt(PriorityTask::getEffectivePriority).reversed());

        // Start task processing in a separate thread
        this.taskProcessor = new Thread(this::processTasks);
        this.taskProcessor.setDaemon(true);
        this.taskProcessor.start();
    }

    public synchronized void submitRequest(int priority, Runnable task) throws InterruptedException {
        taskQueue.offer(new PriorityTask(priority, task));
        notify(); // Wake up the task processor
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

    private synchronized void processTasks() {
        while (true) {
            try {
                while (taskQueue.isEmpty()) {
                    wait(); // Wait for tasks to arrive
                }

                // Remove the highest-priority task
                PriorityTask task = taskQueue.poll();
                if (task != null) {
                    // Execute the task in a separate thread to prevent blocking
                    new Thread(() -> {
                        try {
                            task.getTask().run();
                        } catch (Exception e) {
                            System.err.println("Task execution error: " + e.getMessage());
                        }
                    }).start();

                    // Update aging for remaining tasks
                    taskQueue.forEach(PriorityTask::applyAging);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Task processing interrupted: " + e.getMessage());
            }
        }
    }

    private static class PriorityTask {
        private static final int AGING_INCREMENT = 1; // Increment priority value by 1 per cycle
        private int priority; // Original priority of the task
        private final Runnable task;

        public PriorityTask(int priority, Runnable task) {
            this.priority = priority;
            this.task = task;
        }

        public int getEffectivePriority() {
            return priority;
        }

        public Runnable getTask() {
            return task;
        }

        public void applyAging() {
            this.priority -= AGING_INCREMENT;
        }
    }
}
