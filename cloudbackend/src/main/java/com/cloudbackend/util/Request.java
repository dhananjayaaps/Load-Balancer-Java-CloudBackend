package com.cloudbackend.util;

import java.util.concurrent.atomic.AtomicInteger;

public class Request implements Comparable<Request> {
    private static final AtomicInteger idCounter = new AtomicInteger(0);
    private final int id;
    private int priority;
    private long timestamp;

    public Request(int priority) {
        this.id = idCounter.incrementAndGet();
        this.priority = priority;
        this.timestamp = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public void incrementPriority() {
        this.priority++;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int compareTo(Request other) {
        return Integer.compare(other.priority, this.priority); // Higher priority first
    }
}
