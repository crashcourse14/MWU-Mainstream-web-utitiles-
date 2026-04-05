package com.mwu;

import com.mwu.logger.Logger;
import com.mwu.middleware.*;
import com.mwu.routing.*;
import com.mwu.config.MWUConfig;
import com.mwu.maintenance.MaintenanceManager;
import com.mwu.maintenance.MaintenanceMiddleware;
import com.utils.TrafficMonitor;
import com.utils.Settings;
import com.mwu.setters.PortSetter;
import com.mwu.setters.HostSetter;
import com.mwu.setters.PublicDirSetter;
import com.mwu.middleware.*;
import com.mwu.routing.ServerRoute;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * MWU (Multipurpose Web Utilities) - Modern Java Web Framework
 * 
 * Revolutionary features:
 * - Fluent builder API
 * - Middleware pipeline
 * - Advanced routing with pattern matching
 * - WebSocket support
 * - Reactive request handling
 * - Built-in security features
 * - Comprehensive monitoring
 */
public class MWU {
    private static final Logger logger = new Logger();
    
    // Core configuration
    private MWUConfig config;
    private int port = 8080;
    private String host = "0.0.0.0";
    private String publicDirectory = "public";
    private boolean running = false;
    
    // Advanced features
    private final Router router;
    private final MiddlewareChain middlewareChain;
    private final TrafficMonitor trafficMonitor;
    private ExecutorService threadPool;
    private ServerSocket serverSocket;
    
    // Plugin system
    private final Map<String, Plugin> plugins = new ConcurrentHashMap<>();
    private final List<LifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<>();
    
    // Metrics and monitoring
    private final MetricsCollector metricsCollector;
    private HealthCheckRegistry healthCheckRegistry;
    
    // Maintenance mode
    private MaintenanceManager maintenanceManager;
    
    public MWU() {
        this.config = MWUConfig.builder().build();
        this.router = new Router();
        this.middlewareChain = new MiddlewareChain();
        this.trafficMonitor = new TrafficMonitor();
        this.metricsCollector = new MetricsCollector();
        this.healthCheckRegistry = new HealthCheckRegistry();
        this.maintenanceManager = new MaintenanceManager();
        
        // Register default middleware
        registerDefaultMiddleware();
        
        logger.info("MWU Framework initialized");
    }
    
    private void registerDefaultMiddleware() {
        // Maintenance middleware (check first)
        use(new MaintenanceMiddleware(maintenanceManager));
        
        // Logging middleware
        use(new LoggingMiddleware());
        
        // Security headers
        use(new SecurityHeadersMiddleware());
        
        // Request ID tracking
        use(new RequestIdMiddleware());
    }
    
    /**
     * Builder pattern for fluent configuration
     */
    public static class Builder {
        private final MWU mwu = new MWU();
        
        public Builder port(int port) {
            mwu.port = port;
            return this;
        }
        
        public Builder host(String host) {
            mwu.host = host;
            return this;
        }
        
        public Builder publicDirectory(String dir) {
            mwu.publicDirectory = dir;
            return this;
        }
        
        public Builder threadPoolSize(int size) {
            mwu.config.setThreadPoolSize(size);
            return this;
        }
        
        public Builder enableCors() {
            mwu.use(new CorsMiddleware());
            return this;
        }
        
        public Builder enableCompression() {
            mwu.use(new CompressionMiddleware());
            return this;
        }
        
        public Builder rateLimit(int requestsPerMinute) {
            mwu.use(new RateLimitMiddleware(requestsPerMinute));
            return this;
        }
        
        public Builder config(MWUConfig config) {
            mwu.config = config;
            return this;
        }
        
        public MWU build() {
            return mwu;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // ==================== Routing API ====================
    
    public MWU get(String path, RouteHandler handler) {
        router.addRoute("GET", path, handler);
        return this;
    }
    
    public MWU post(String path, RouteHandler handler) {
        router.addRoute("POST", path, handler);
        return this;
    }
    
    public MWU put(String path, RouteHandler handler) {
        router.addRoute("PUT", path, handler);
        return this;
    }
    
    public MWU delete(String path, RouteHandler handler) {
        router.addRoute("DELETE", path, handler);
        return this;
    }
    
    public MWU patch(String path, RouteHandler handler) {
        router.addRoute("PATCH", path, handler);
        return this;
    }
    
    public MWU route(String method, String path, RouteHandler handler) {
        router.addRoute(method, path, handler);
        return this;
    }
    
    // Group routes with common prefix
    public MWU group(String prefix, Consumer<Router> routerConfig) {
        Router groupRouter = router.group(prefix);
        routerConfig.accept(groupRouter);
        return this;
    }
    
    // ==================== Middleware API ====================
    
    public MWU use(Middleware middleware) {
        middlewareChain.add(middleware);
        return this;
    }
    
    public MWU use(String path, Middleware middleware) {
        middlewareChain.add(new PathFilteredMiddleware(path, middleware));
        return this;
    }
    
    // ==================== Static Files ====================
    
    public MWU staticFiles(String directory) {
        this.publicDirectory = directory;
        return this;
    }
    
    public MWU staticFiles(String mountPath, String directory) {
        use(mountPath, new StaticFileMiddleware(directory));
        return this;
    }
    
    // ==================== WebSocket Support ====================
    
    public MWU websocket(String path, WebSocketHandler handler) {
        router.addWebSocketRoute(path, handler);
        return this;
    }
    
    // ==================== Plugin System ====================
    
    public MWU registerPlugin(String name, Plugin plugin) {
        plugins.put(name, plugin);
        plugin.initialize(this);
        logger.info("Plugin registered: " + name);
        return this;
    }
    
    public <T extends Plugin> T getPlugin(String name, Class<T> type) {
        Plugin plugin = plugins.get(name);
        if (plugin != null && type.isInstance(plugin)) {
            return type.cast(plugin);
        }
        return null;
    }
    
    // ==================== Lifecycle Management ====================
    
    public MWU onStart(LifecycleListener.StartListener listener) {
        lifecycleListeners.add(new LifecycleListener() {
            @Override
            public void onStart() {
                listener.onStart();
            }
        });
        return this;
    }
    
    public MWU onStop(LifecycleListener.StopListener listener) {
        lifecycleListeners.add(new LifecycleListener() {
            @Override
            public void onStop() {
                listener.onStop();
            }
        });
        return this;
    }
    
    public MWU onError(BiConsumer<Request, Throwable> errorHandler) {
        config.setErrorHandler(errorHandler);
        return this;
    }
    
    // ==================== Health Checks ====================
    
    public MWU registerHealthCheck(String name, HealthCheck check) {
        healthCheckRegistry.register(name, check);
        return this;
    }
    
    // ==================== Server Lifecycle ====================
    
    public void start() throws IOException {
        if (running) {
            throw new IllegalStateException("Server is already running");
        }
        
        logger.info("Starting MWU Framework...");
        
        // Initialize thread pool
        int poolSize = config.getThreadPoolSize();
        threadPool = Executors.newFixedThreadPool(poolSize);
        logger.info("Thread pool initialized with " + poolSize + " threads");
        
        // Load settings if available
        loadSettings();
        
        // Create server socket
        serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));
        running = true;
        
        // Notify lifecycle listeners
        lifecycleListeners.forEach(LifecycleListener::onStart);
        
        // Register default routes
        registerDefaultRoutes();
        
        logger.info("✓ Server started successfully");
        logger.info("✓ Listening on http://" + host + ":" + port);
        logger.info("✓ Public directory: " + publicDirectory);
        logger.info("✓ Middleware chain: " + middlewareChain.size() + " middleware(s)");
        logger.info("✓ Routes registered: " + router.getRouteCount());
        
        // Accept connections
        acceptConnections();
    }
    
    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(() -> handleConnection(clientSocket));
            } catch (IOException e) {
                if (running) {
                    logger.error("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
    
    private void handleConnection(Socket socket) {
        long startTime = System.currentTimeMillis();
        Request request = null;
        Response response = null;
        
        try {
            // Parse request
            request = Request.parse(socket.getInputStream(), socket);
            response = new Response(socket.getOutputStream());
            
            // Make effectively final for lambda
            final Request finalRequest = request;
            final Response finalResponse = response;
            
            // Record traffic
            if (config.isTrafficMonitoringEnabled()) {
                trafficMonitor.recordRequest(
                    finalRequest.getPath(),
                    finalRequest.getClientIP(),
                    finalRequest.getMethod(),
                    finalRequest.getHeader("User-Agent")
                );
            }
            
            // Execute middleware chain and route handler
            middlewareChain.execute(finalRequest, finalResponse, () -> {
                // Try to match route
                try {
                    ServerRoute route = router.match(finalRequest.getMethod(), finalRequest.getPath());

                    if (route != null) {
                        finalRequest.setParams(route.getParams());
                        route.getHandler().handle(finalRequest, finalResponse);

                    } else if ("/".equals(finalRequest.getPath())) {
                        // Serve index.html for root path
                        serveIndexFile(finalRequest, finalResponse);

                    } else if (isStaticFileRequest(finalRequest)) {
                        serveStaticFile(finalRequest, finalResponse);

                    } else {
                        // Serve 404.html for invalid paths
                        serve404File(finalRequest, finalResponse);
                    }

                } catch (Exception e) {
                    try {
                        finalResponse.status(500).send("Internal Server Error");
                    } catch (IOException ignored) {
                        // fallback
                    }
                }
            });

            
            // Ensure response is sent
            if (!finalResponse.isSent()) {
                finalResponse.send();
            }
            
        } catch (Exception e) {
            logger.error("Error handling request: " + e.getMessage());
            
            if (response != null && !response.isSent()) {
                try {
                    if (config.getErrorHandler() != null) {
                        config.getErrorHandler().accept(request, e);
                    }
                    response.status(500).send("Internal Server Error");
                } catch (Exception ex) {
                    logger.error("Error sending error response: " + ex.getMessage());
                }
            }
        } finally {
            // Record metrics
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (response != null && config.isTrafficMonitoringEnabled()) {
                trafficMonitor.recordResponse(
                    response.getStatusCode(),
                    responseTime,
                    response.getBytesSent()
                );
            }
            
            metricsCollector.recordRequest(responseTime);
            
            // Close socket
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("Error closing socket: " + e.getMessage());
            }
        }
    }
    
    private void registerDefaultRoutes() {
        // Health check endpoint
        get("/_health", (req, res) -> {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "healthy");
            health.put("uptime", metricsCollector.getUptime());
            health.put("checks", healthCheckRegistry.runAll());
            res.json(health);
        });
        
        // Metrics endpoint
        get("/_metrics", (req, res) -> {
            if (config.isTrafficMonitoringEnabled()) {
                res.json(convertTrafficStats(trafficMonitor.getStats()));
            } else {
                res.json(metricsCollector.getMetrics());
            }
        });
        
        // Server info endpoint
        get("/_info", (req, res) -> {
            Map<String, Object> info = new HashMap<>();
            info.put("framework", "MWU");
            info.put("version", "2.0.0");
            info.put("routes", router.getRouteCount());
            info.put("middleware", middlewareChain.size());
            info.put("plugins", plugins.keySet());
            res.json(info);
        });
    }
    
    private boolean isStaticFileRequest(Request request) {
        if (publicDirectory == null) return false;
        
        String path = request.getPath();
        Path filePath = Paths.get(publicDirectory, path);
        
        return Files.exists(filePath) && Files.isRegularFile(filePath);
    }
    
    private void serveIndexFile(Request request, Response response) throws IOException {
        Path filePath = Paths.get(publicDirectory, "index.html");
        
        if (!Files.exists(filePath)) {
            response.status(404).send("<h1>404 - Index Not Found</h1>");
            return;
        }
        
        String contentType = "text/html";
        byte[] content = Files.readAllBytes(filePath);
        response.header("Content-Type", contentType)
                .header("Content-Length", String.valueOf(content.length))
                .send(content);
    }
    
    private void serve404File(Request request, Response response) throws IOException {
        Path filePath = Paths.get(publicDirectory, "404.html");
        response.status(404);
        
        if (Files.exists(filePath)) {
            String contentType = "text/html";
            byte[] content = Files.readAllBytes(filePath);
            response.header("Content-Type", contentType)
                    .header("Content-Length", String.valueOf(content.length))
                    .send(content);
        } else {
            response.send("<h1>404 - Page Not Found</h1>");
        }
    }
    
    private void serveStaticFile(Request request, Response response) throws IOException {
        Path filePath = Paths.get(publicDirectory, request.getPath());
        
        if (!filePath.normalize().startsWith(Paths.get(publicDirectory).normalize())) {
            response.status(403).send("Forbidden");
            return;
        }
        
        if (!Files.exists(filePath)) {
            response.status(404).send("Not Found");
            return;
        }
        
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        
        byte[] content = Files.readAllBytes(filePath);
        response.header("Content-Type", contentType)
                .header("Content-Length", String.valueOf(content.length))
                .send(content);
    }
    
    private Map<String, Object> convertTrafficStats(TrafficMonitor.TrafficStats stats) {
        Map<String, Object> result = new HashMap<>();
        result.put("total_requests", stats.totalRequests);
        result.put("unique_ips", stats.uniqueIPs);
        result.put("active_connections", stats.activeConnections);
        result.put("peak_connections", stats.peakConnections);
        result.put("avg_response_time_ms", stats.avgResponseTime);
        result.put("min_response_time_ms", stats.minResponseTime);
        result.put("max_response_time_ms", stats.maxResponseTime);
        result.put("median_response_time_ms", stats.medianResponseTime);
        result.put("p95_response_time_ms", stats.p95ResponseTime);
        result.put("total_bytes_sent", stats.totalBytesSent);
        result.put("top_paths", stats.topPaths);
        result.put("requests_by_method", stats.requestsByMethod);
        result.put("status_codes", stats.requestsByStatusCode);
        result.put("server_uptime", stats.startTime.toString());
        return result;
    }
    
    private void loadSettings() {
        try {
            Settings settings = new Settings();
            
            if (settings.getPort() > 0) {
                this.port = settings.getPort();
            }
            
            if (settings.getHost() != null && !settings.getHost().isEmpty()) {
                this.host = settings.getHost();
            }
            
            config.setTrafficMonitoringEnabled(settings.isTrafficMonitoringEnabled());
            
            // Check for maintenance mode in settings.json
            try {
                String settingsJson = new String(Files.readAllBytes(Paths.get("settings.json")));
                if (settingsJson.contains("\"maintenance\": true")) {
                    maintenanceManager.enableMaintenanceMode();
                    logger.info("Maintenance mode enabled from settings.json");
                }
            } catch (Exception e) {
                // Maintenance JSON check failed, continue
            }
            
            logger.info("Settings loaded from settings.json");
        } catch (Exception e) {
            logger.warn("Could not load settings.json, using defaults");
        }
    }
    
    public void stop() {
        if (!running) {
            return;
        }
        
        logger.info("Stopping MWU Framework...");
        running = false;
        
        // Notify lifecycle listeners
        lifecycleListeners.forEach(LifecycleListener::onStop);
        
        // Shutdown thread pool
        if (threadPool != null) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
        }
        
        // Close server socket
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket: " + e.getMessage());
        }
        
        logger.info("✓ Server stopped");
    }
    
    // ==================== Legacy API Support ====================
    
    @Deprecated
    public PortSetter port() {
        return new PortSetter(this);
    }
    
    @Deprecated
    public HostSetter host() {
        return new HostSetter(this);
    }
    
    @Deprecated
    public PublicDirSetter dir() {
        return new PublicDirSetter(this);
    }
    
    public void setPortInternal(int port) {
        this.port = port;
    }
    
    public void setHostInternal(String host) {
        this.host = host;
    }
    
    public void setPublicDirInternal(String dir) {
        this.publicDirectory = dir;
    }

    private TrafficStats trafficStats = new TrafficStats();

    public TrafficMonitor.TrafficStats getTrafficStats() {
        return trafficMonitor.getStats();
    }
    
    // ==================== Getters ====================
    
    public int getPort() {
        return port;
    }
    
    public String getHost() {
        return host;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public TrafficMonitor getTrafficMonitor() {
        return trafficMonitor;
    }
    
    public Router getRouter() {
        return router;
    }
    
    public MWUConfig getConfig() {
        return config;
    }
}