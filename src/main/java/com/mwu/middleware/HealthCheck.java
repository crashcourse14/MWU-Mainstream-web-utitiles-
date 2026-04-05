package com.mwu.middleware;

public interface HealthCheck {
    HealthStatus check();
    
    class HealthStatus {
        private final boolean healthy;
        private final String message;
        
        public HealthStatus(boolean healthy, String message) {
            this.healthy = healthy;
            this.message = message;
        }
        
        public static HealthStatus healthy() {
            return new HealthStatus(true, "OK");
        }
        
        public static HealthStatus healthy(String message) {
            return new HealthStatus(true, message);
        }
        
        public static HealthStatus unhealthy(String message) {
            return new HealthStatus(false, message);
        }
        
        public boolean isHealthy() {
            return healthy;
        }
        
        public String getMessage() {
            return message;
        }
    }
}