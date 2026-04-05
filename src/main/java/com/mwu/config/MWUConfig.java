package com.mwu.config;

import com.mwu.routing.Request;

import java.util.function.BiConsumer;

/**
 * MWU Framework Configuration
 */
public class MWUConfig {
    private int threadPoolSize = 50;
    private boolean trafficMonitoringEnabled = true;
    private int maxRequestSize = 10 * 1024 * 1024; // 10MB
    private int connectionTimeout = 30000; // 30 seconds
    private BiConsumer<Request, Throwable> errorHandler;
    
    private MWUConfig() {}
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final MWUConfig config = new MWUConfig();
        
        public Builder threadPoolSize(int size) {
            config.threadPoolSize = size;
            return this;
        }
        
        public Builder trafficMonitoringEnabled(boolean enabled) {
            config.trafficMonitoringEnabled = enabled;
            return this;
        }
        
        public Builder maxRequestSize(int bytes) {
            config.maxRequestSize = bytes;
            return this;
        }
        
        public Builder connectionTimeout(int ms) {
            config.connectionTimeout = ms;
            return this;
        }
        
        public Builder errorHandler(BiConsumer<Request, Throwable> handler) {
            config.errorHandler = handler;
            return this;
        }
        
        public MWUConfig build() {
            return config;
        }
    }
    
    // Getters
    public int getThreadPoolSize() {
        return threadPoolSize;
    }
    
    public boolean isTrafficMonitoringEnabled() {
        return trafficMonitoringEnabled;
    }
    
    public int getMaxRequestSize() {
        return maxRequestSize;
    }
    
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public BiConsumer<Request, Throwable> getErrorHandler() {
        return errorHandler;
    }
    
    // Setters (for legacy Settings compatibility)
    public void setThreadPoolSize(int size) {
        this.threadPoolSize = size;
    }
    
    public void setTrafficMonitoringEnabled(boolean enabled) {
        this.trafficMonitoringEnabled = enabled;
    }
    
    public void setErrorHandler(BiConsumer<Request, Throwable> handler) {
        this.errorHandler = handler;
    }
}