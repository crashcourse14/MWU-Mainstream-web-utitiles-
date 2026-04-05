package com.server;

import com.mwu.MWU;
import com.mwu.logger.Logger;
import com.mwu.middleware.*;
import com.mwu.config.MWUConfig;
import com.mwu.util.CommandListener;

import java.util.*;

/**
 * Modern MWU Server Example
 * Demonstrates revolutionary features and best practices
 */
public class ModernServer {
    public static void main(String[] args) {
        Logger logger = new Logger();
        
        logger.info("=================================================");
        logger.info("  MWU Framework 2.0 - Revolutionary Edition");
        logger.info("=================================================");
        
        // Modern builder pattern configuration
        MWU server = MWU.builder()
            .port(8080)
            .host("0.0.0.0")
            .publicDirectory("public")
            .threadPoolSize(100)
            .enableCors()
            .rateLimit(100) // 100 requests per minute
            .config(MWUConfig.builder()
                .trafficMonitoringEnabled(true)
                .maxRequestSize(10 * 1024 * 1024) // 10MB
                .errorHandler((req, err) -> {
                    logger.error("Request failed: " + req.getPath() + " - " + err.getMessage());
                })
                .build())
            .build();
        
        // ==================== REST API Routes ====================
        
        // Basic routes
        server.get("/", (req, res) -> {
        res.html(
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <title>MWU Framework 2.0</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\n" +
            "            max-width: 800px;\n" +
            "            margin: 50px auto;\n" +
            "            padding: 20px;\n" +
            "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
            "            color: white;\n" +
            "        }\n" +
            "        h1 { font-size: 3em; margin-bottom: 10px; }\n" +
            "        .feature {\n" +
            "            background: rgba(255,255,255,0.1);\n" +
            "            padding: 15px;\n" +
            "            margin: 10px 0;\n" +
            "            border-radius: 8px;\n" +
            "        }\n" +
            "        a { color: #fff; text-decoration: none; border-bottom: 2px solid #fff; }\n" +
            "        code { background: rgba(0,0,0,0.2); padding: 2px 8px; border-radius: 4px; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <h1>🚀 MWU Framework 2.0</h1>\n" +
            "    <p>Revolutionary Java Web Framework</p>\n" +
            "    <div class=\"feature\">\n" +
            "        <h3>✨ Modern Features</h3>\n" +
            "        <ul>\n" +
            "            <li>Fluent builder API</li>\n" +
            "            <li>Middleware pipeline</li>\n" +
            "            <li>Advanced routing with params</li>\n" +
            "            <li>Built-in rate limiting</li>\n" +
            "            <li>Comprehensive monitoring</li>\n" +
            "        </ul>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>"
        );
    });
        
        server.get("/api/hello", (req, res) -> {
            String name = req.getQueryParam("name", "World");
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Hello, " + name + "!");
            response.put("timestamp", System.currentTimeMillis());
            response.put("requestId", req.getAttribute("requestId"));
            
            res.json(response);
        });
        
        // Route with path parameters
        server.get("/api/users/:id", (req, res) -> {
            String userId = req.getPathParam("id");
            
            Map<String, Object> user = new HashMap<>();
            user.put("id", userId);
            user.put("name", "User " + userId);
            user.put("email", "user" + userId + "@example.com");
            
            res.json(user);
        });
        
        // POST endpoint
        server.post("/api/users", (req, res) -> {
            String body = req.getBodyAsString();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "created");
            response.put("data", body);
            response.put("id", UUID.randomUUID().toString());
            
            res.status(201).json(response);
        });
        
        // ==================== Route Groups ====================
        
        server.group("/api/v2", router -> {
            router.addRoute("GET", "/status", (req, res) -> {
                res.json(Map.of("version", "2.0", "status", "operational"));
            });
            
            router.addRoute("GET", "/features", (req, res) -> {
                res.json(Arrays.asList(
                    "Builder Pattern",
                    "Middleware Chain",
                    "Advanced Routing",
                    "Rate Limiting",
                    "Health Checks",
                    "Metrics Collection",
                    "Plugin System"
                ));
            });
        });
        
        // ==================== Custom Middleware ====================
        
        server.use((req, res, next) -> {
            // Add custom header to all responses
            res.header("X-Powered-By", "MWU/2.0");
            next.run();
        });
        
        // Path-specific middleware
        server.use("/api", (req, res, next) -> {
            // API-specific logic
            res.header("X-API-Version", "2.0");
            next.run();
        });
        
        // ==================== Health Checks ====================
        
        server.registerHealthCheck("database", () -> {
            // Simulate database check
            return HealthCheck.HealthStatus.healthy("Database connected");
        });
        
        server.registerHealthCheck("external-api", () -> {
            // Simulate external API check
            return Math.random() > 0.1 
                ? HealthCheck.HealthStatus.healthy()
                : HealthCheck.HealthStatus.unhealthy("API timeout");
        });
        
        // ==================== Lifecycle Hooks ====================
        
        server.onStart(() -> {
            logger.info("🎉 Server started successfully!");
            logger.info("💡 Try these URLs:");
            logger.info("   http://localhost:8080/");
            logger.info("   http://localhost:8080/api/hello?name=MWU");
            logger.info("   http://localhost:8080/api/users/123");
            logger.info("   http://localhost:8080/_metrics");
        });
        
        server.onStop(() -> {
            logger.info("👋 Server shutting down gracefully...");
            // Cleanup resources
        });
        
        // ==================== Error Handling ====================
        
        server.get("/api/error", (req, res) -> {
            throw new RuntimeException("Intentional error for testing");
        });
        
        // ==================== Start Server ====================
        
        try {
            System.out.println("TRY BLOCK: Entered try block");
            
            // Start command listener for terminal control BEFORE starting server
            System.out.println("MAIN: Creating CommandListener...");
            CommandListener commandListener = new CommandListener(server);
            System.out.println("MAIN: Creating thread...");
            Thread commandThread = new Thread(commandListener, "CommandListener");
            System.out.println("MAIN: Setting daemon to false...");
            commandThread.setDaemon(false);
            System.out.println("MAIN: Starting thread...");
            commandThread.start();
            System.out.println("MAIN: CommandListener thread started!");
            
            server.start();
            
        } catch (Exception e) {
            logger.error("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}