package com.cloudbackend.util;

public class PriorityRequest implements Comparable<PriorityRequest> {
    private final int requestId;
    private int priority;
    private final long timestamp;
    private final Runnable task;

    public PriorityRequest(int requestId, int priority, long timestamp, Runnable task) {
        this.requestId = requestId;
        this.priority = priority;
        this.timestamp = timestamp;
        this.task = task;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Runnable getTask() {
        return task;
    }

    @Override
    public int compareTo(PriorityRequest other) {
        return Integer.compare(this.priority, other.priority);
    }
}