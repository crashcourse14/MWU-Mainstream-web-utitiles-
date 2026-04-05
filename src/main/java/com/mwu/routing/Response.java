package com.mwu.routing;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * HTTP Response with fluent API
 */
public class Response {
    private final OutputStream output;
    private int statusCode = 200;
    private String statusMessage = "OK";
    private Map<String, String> headers;
    private boolean sent = false;
    private long bytesSent = 0;
    
    private static final Map<Integer, String> STATUS_MESSAGES = new HashMap<>();
    
    static {
        STATUS_MESSAGES.put(200, "OK");
        STATUS_MESSAGES.put(201, "Created");
        STATUS_MESSAGES.put(204, "No Content");
        STATUS_MESSAGES.put(301, "Moved Permanently");
        STATUS_MESSAGES.put(302, "Found");
        STATUS_MESSAGES.put(304, "Not Modified");
        STATUS_MESSAGES.put(400, "Bad Request");
        STATUS_MESSAGES.put(401, "Unauthorized");
        STATUS_MESSAGES.put(403, "Forbidden");
        STATUS_MESSAGES.put(404, "Not Found");
        STATUS_MESSAGES.put(405, "Method Not Allowed");
        STATUS_MESSAGES.put(429, "Too Many Requests");
        STATUS_MESSAGES.put(500, "Internal Server Error");
        STATUS_MESSAGES.put(502, "Bad Gateway");
        STATUS_MESSAGES.put(503, "Service Unavailable");
    }
    
    public Response(OutputStream output) {
        this.output = output;
        this.headers = new LinkedHashMap<>();
        
        // Default headers
        header("Server", "MWU/2.0");
        header("Date", new Date().toString());
    }
    
    // ==================== Status ====================
    
    public Response status(int code) {
        this.statusCode = code;
        this.statusMessage = STATUS_MESSAGES.getOrDefault(code, "Unknown");
        return this;
    }
    
    public Response status(int code, String message) {
        this.statusCode = code;
        this.statusMessage = message;
        return this;
    }
    
    // ==================== Headers ====================
    
    public Response header(String name, String value) {
        headers.put(name, value);
        return this;
    }
    
    public Response headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }
    
    public Response contentType(String type) {
        return header("Content-Type", type);
    }
    
    // ==================== Send Methods ====================
    
    public void send() throws IOException {
        send(new byte[0]);
    }
    
    public void send(String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        header("Content-Type", "text/html; charset=UTF-8");
        send(bytes);
    }
    
    public void send(byte[] content) throws IOException {
        if (sent) {
            throw new IllegalStateException("Response already sent");
        }
        
        header("Content-Length", String.valueOf(content.length));
        
        // Write status line
        String statusLine = String.format("HTTP/1.1 %d %s\r\n", statusCode, statusMessage);
        output.write(statusLine.getBytes(StandardCharsets.UTF_8));
        
        // Write headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String headerLine = String.format("%s: %s\r\n", entry.getKey(), entry.getValue());
            output.write(headerLine.getBytes(StandardCharsets.UTF_8));
        }
        
        // Blank line
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
        
        // Write body
        if (content.length > 0) {
            output.write(content);
        }
        
        output.flush();
        sent = true;
        bytesSent = content.length;
    }
    
    // ==================== Convenience Methods ====================
    
    public void json(Object data) throws IOException {
        String json;
        
        if (data instanceof String) {
            json = (String) data;
        } else if (data instanceof Map || data instanceof List) {
            json = simpleJsonSerializer(data);
        } else {
            json = data.toString();
        }
        
        contentType("application/json; charset=UTF-8");
        send(json);
    }
    
    public void html(String html) throws IOException {
        contentType("text/html; charset=UTF-8");
        send(html);
    }
    
    public void text(String text) throws IOException {
        contentType("text/plain; charset=UTF-8");
        send(text);
    }
    
    public void redirect(String location) throws IOException {
        status(302);
        header("Location", location);
        send();
    }
    
    public void redirect(int statusCode, String location) throws IOException {
        status(statusCode);
        header("Location", location);
        send();
    }
    
    public void notFound() throws IOException {
        status(404);
        html("<html><body><h1>404 Not Found</h1></body></html>");
    }
    
    public void error(int statusCode, String message) throws IOException {
        status(statusCode);
        html(String.format("<html><body><h1>%d %s</h1><p>%s</p></body></html>", 
            statusCode, statusMessage, message));
    }
    
    // ==================== File Download ====================
    
    public void download(byte[] data, String filename) throws IOException {
        header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        header("Content-Type", "application/octet-stream");
        send(data);
    }
    
    // ==================== Getters ====================
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public boolean isSent() {
        return sent;
    }
    
    public long getBytesSent() {
        return bytesSent;
    }
    
    // ==================== Simple JSON Serializer ====================
    
    private String simpleJsonSerializer(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        if (obj instanceof String) {
            return "\"" + escapeJson((String) obj) + "\"";
        }
        
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        
        if (obj instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            Map<?, ?> map = (Map<?, ?>) obj;
            boolean first = true;
            
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(escapeJson(entry.getKey().toString())).append("\":");
                sb.append(simpleJsonSerializer(entry.getValue()));
                first = false;
            }
            
            sb.append("}");
            return sb.toString();
        }
        
        if (obj instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            List<?> list = (List<?>) obj;
            boolean first = true;
            
            for (Object item : list) {
                if (!first) sb.append(",");
                sb.append(simpleJsonSerializer(item));
                first = false;
            }
            
            sb.append("]");
            return sb.toString();
        }
        
        // Fallback: try to serialize as object
        return "\"" + escapeJson(obj.toString()) + "\"";
    }
    
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}