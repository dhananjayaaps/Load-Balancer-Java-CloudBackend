package com.cloudbackend.service;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MetricsService {
    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordRequest(String container) {
        meterRegistry.counter("requests.total", "container", container).increment();
    }

    public void recordLatency(String container, long durationMs) {
        meterRegistry.timer("requests.latency", "container", container).record(durationMs, TimeUnit.MILLISECONDS);
    }
}
