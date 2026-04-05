package com.mwu.maintenance;

import com.mwu.logger.Logger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MaintenanceManager {
    private static final Logger logger = new Logger();
    private boolean maintenanceMode = false;
    private LocalDateTime maintenanceStartTime;
    private LocalDateTime estimatedEndTime;
    private String maintenanceMessage = "Server is under maintenance. Please try again later.";
    
    public MaintenanceManager() {
    }
    
    public void enableMaintenanceMode() {
        this.maintenanceMode = true;
        this.maintenanceStartTime = LocalDateTime.now();
        logger.info("Maintenance mode enabled at " + maintenanceStartTime);
    }
    
    public void disableMaintenanceMode() {
        this.maintenanceMode = false;
        logger.info("Maintenance mode disabled");
    }
    
    public void setMaintenanceMessage(String message) {
        this.maintenanceMessage = message;
    }
    
    public void setEstimatedEndTime(LocalDateTime endTime) {
        this.estimatedEndTime = endTime;
    }
    
    public boolean isMaintenanceModeEnabled() {
        return maintenanceMode;
    }
    
    public LocalDateTime getMaintenanceStartTime() {
        return maintenanceStartTime;
    }
    
    public LocalDateTime getEstimatedEndTime() {
        return estimatedEndTime;
    }
    
    public String getMaintenanceMessage() {
        return maintenanceMessage;
    }
    
    public String getMaintenanceInfo() {
        if (!maintenanceMode) {
            return "";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Maintenance Mode: ACTIVE\n");
        info.append("Started: ").append(maintenanceStartTime).append("\n");
        if (estimatedEndTime != null) {
            info.append("Estimated End: ").append(estimatedEndTime).append("\n");
        }
        info.append("Message: ").append(maintenanceMessage).append("\n");
        return info.toString();
    }
}
