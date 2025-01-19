package com.cloudbackend.util;

import java.util.concurrent.atomic.AtomicLong;

public class Request {
    private static final AtomicLong ID_GENERATOR = new AtomicLong();
    private final long id; // Unique ID
    private final long timestamp; // Time the request was created
    private int priority; // Current priority of the request
    private final String operation; // Upload, download, etc.

    public Request(int initialPriority, String operation) {
        this.id = ID_GENERATOR.incrementAndGet();
        this.timestamp = System.currentTimeMillis();
        this.priority = initialPriority;
        this.operation = operation;
    }

    public long getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getPriority() {
        return priority;
    }

    public String getOperation() {
        return operation;
    }

    public void increasePriority(int increment) {
        this.priority += increment;
    }
}
