package com.mwu.middleware;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class MetricsCollector {
    private final Instant startTime = Instant.now();
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    
    public void recordRequest(long responseTimeMs) {
        totalRequests.incrementAndGet();
        totalResponseTime.addAndGet(responseTimeMs);
    }
    
    public long getUptime() {
        return Duration.between(startTime, Instant.now()).getSeconds();
    }
    
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        long requests = totalRequests.get();
        
        metrics.put("uptime_seconds", getUptime());
        metrics.put("total_requests", requests);
        metrics.put("avg_response_time_ms", 
            requests > 0 ? totalResponseTime.get() / requests : 0);
        
        return metrics;
    }
}