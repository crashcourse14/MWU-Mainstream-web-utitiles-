package com.mwu.middleware;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HealthCheckRegistry {
    private final Map<String, HealthCheck> checks = new ConcurrentHashMap<>();
    
    public void register(String name, HealthCheck check) {
        checks.put(name, check);
    }
    
    public Map<String, Object> runAll() {
        Map<String, Object> results = new HashMap<>();
        
        for (Map.Entry<String, HealthCheck> entry : checks.entrySet()) {
            try {
                HealthCheck.HealthStatus status = entry.getValue().check();
                Map<String, Object> checkResult = new HashMap<>();
                checkResult.put("healthy", status.isHealthy());
                checkResult.put("message", status.getMessage());
                results.put(entry.getKey(), checkResult);
            } catch (Exception e) {
                Map<String, Object> checkResult = new HashMap<>();
                checkResult.put("healthy", false);
                checkResult.put("message", "Error: " + e.getMessage());
                results.put(entry.getKey(), checkResult);
            }
        }
        
        return results;
    }
}