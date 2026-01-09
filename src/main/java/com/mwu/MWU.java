package com.mwu;

import com.mwu.logger.Logger;
import com.functions.NotFoundHandler;
import com.utils.Settings;
import com.mwu.setters.HostSetter;
import com.mwu.setters.PortSetter;
import com.mwu.setters.PublicDirSetter;
import com.utils.hardware.CPU;
//import com.utils.hardware.GPU;
import com.utils.hardware.RAM;
import com.utils.hardware.StorageCheck;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;




public class MWU {

    private String host = "localhost";
    private int port = 8080;
    private String publicDir = "public";
    private HttpServer server;
    private Logger logger = new Logger();

    private Settings settings;

    // Delegate setter classes
    private PortSetter portSetter = new PortSetter(this);
    private PublicDirSetter dirSetter = new PublicDirSetter(this);
    private HostSetter hostSetter = new HostSetter(this);
    private NotFoundHandler notFoundHandler = null;
    private String notFoundFile = "404.html"; 

    public HostSetter host() { return hostSetter; }
    public PortSetter port() { return portSetter; }
    public PublicDirSetter dir() { return dirSetter; }


    // Internal setters used by delegate classes
    public void setPortInternal(int port) { 
        this.port = port; 
        logger.info("Port set to " + port); 
    }

    public void setHostInternal(String host) {
        this.host = host;
        logger.info("Host set to " + host);
    }


    public void setPublicDirInternal(String dir) { 
        this.publicDir = dir; 
        logger.info("Public directory set to " + dir); 
    }

    public void onNotFound(NotFoundHandler handler) {
        this.notFoundHandler = handler;
    }

    public void showUserSpecs() {
        CPU.log();
        RAM.log();
    }

    // MIME types
    private static final Map<String,String> mimeTypes = new HashMap<>();
    static {
        mimeTypes.put("html","text/html");
        mimeTypes.put("css","text/css");
        mimeTypes.put("js","application/javascript");
        mimeTypes.put("png","image/png");
        mimeTypes.put("jpg","image/jpeg");
        mimeTypes.put("jpeg","image/jpeg");
        mimeTypes.put("gif","image/gif");
    }

    
    
    public void start() throws IOException {

        showUserSpecs();
        StorageCheck.check();

        logger.log("Loading  settings...");

        try {
            settings = new Settings();
            logger.info("Settings loaded.");
            logger.info("Maintenance mode: " + settings.isMaintenance());
        } catch (Exception e) {
            logger.warn("Could not load settings.json: " + e.getMessage());
        }

        if (settings.getHost() != null) {
            setHostInternal(settings.getHost());
        }


        logger.log("Starting the server...");
        server = HttpServer.create(new InetSocketAddress(host, port), 0);
        server.createContext("/", this::handleRequest);
        logger.info("Server running at " + host + ":" + port);
        server.start();
    }



    public void stop() {
        if (server != null) {
            logger.info("Shutting the server down...");
            server.stop(1);
        } else {
            logger.error("Could not stop server. No current instances are running.");
        }
    }



    /** Handle HTTP request */
    private void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        // Maintenance mode override
        if (settings != null && settings.isMaintenance()) {
            logger.warn("Maintenance mode active — serving maintenance.html");
            path = "maintenance.html";
        } else {
            if (path.equals("/")) path = "index.html";
            if (path.startsWith("/")) path = path.substring(1);
        }

        Path filePath = Path.of(publicDir, path);

        if (Files.exists(filePath)) {
            // Serve the file normally
            byte[] bytes = Files.readAllBytes(filePath);
            String ext = getExtension(filePath.toString());
            String mime = mimeTypes.getOrDefault(ext, "application/octet-stream");

            exchange.getResponseHeaders().add("Content-Type", mime);
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }

            logger.info("Served file: " + filePath);
            return; 
        }

        Path nfPath = Path.of(publicDir, "404.html");
        if (Files.exists(nfPath)) {
            byte[] bytes = Files.readAllBytes(nfPath);
            exchange.getResponseHeaders().add("Content-Type", "text/html");
            exchange.sendResponseHeaders(404, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
            logger.warn("Served 404.html for missing file: " + path);
            return;
        }
        
        if (notFoundHandler != null) {
            notFoundHandler.handle(exchange);
            logger.info("Executed custom NotFoundHandler for missing file: " + path);
            return;
        }

        exchange.sendResponseHeaders(404, -1);
        logger.warn("File not found and no 404 handler: " + filePath);

    }



    private String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        return (i == -1) ? "" : filename.substring(i+1);
    }
}
