package com.mwu.maintenance;

import com.mwu.middleware.Middleware;
import com.mwu.routing.Request;
import com.mwu.routing.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MaintenanceMiddleware implements Middleware {
    private final MaintenanceManager maintenanceManager;
    
    public MaintenanceMiddleware(MaintenanceManager maintenanceManager) {
        this.maintenanceManager = maintenanceManager;
    }
    
    @Override
    public void handle(Request request, Response response, Runnable next) throws Exception {
        // Allow health check endpoints during maintenance
        if (request.getPath().equals("/health") || request.getPath().equals("/api/health")) {
            next.run();
            return;
        }
        
        if (maintenanceManager.isMaintenanceModeEnabled()) {
            response.status(503);
            response.header("Content-Type", "text/html; charset=UTF-8");
            response.header("Retry-After", "3600");
            
            try {
                String maintenanceHtml = loadMaintenanceHtml();
                response.send(maintenanceHtml);
            } catch (Exception e) {
                response.send("<h1>Service Unavailable</h1><p>" + maintenanceManager.getMaintenanceMessage() + "</p>");
            }
            return;
        }
        
        next.run();
    }
    
    private String loadMaintenanceHtml() throws IOException {
        String htmlPath = "public/maintenance.html";
        if (Files.exists(Paths.get(htmlPath))) {
            return new String(Files.readAllBytes(Paths.get(htmlPath)));
        }
        return getDefaultMaintenanceHtml();
    }
    
    private String getDefaultMaintenanceHtml() {
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "  <title>Maintenance</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "  <h1>" + maintenanceManager.getMaintenanceMessage() + "</h1>\n" +
            "</body>\n" +
            "</html>";
    }
}
