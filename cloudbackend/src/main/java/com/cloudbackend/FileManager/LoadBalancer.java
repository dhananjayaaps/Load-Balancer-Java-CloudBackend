package com.cloudbackend.FileManager;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoadBalancer {

    private final AtomicInteger counter = new AtomicInteger(0);

    public String getNextContainer(List<String> containers) {
        int index = counter.getAndIncrement() % containers.size();
        return containers.get(index);
    }
}
