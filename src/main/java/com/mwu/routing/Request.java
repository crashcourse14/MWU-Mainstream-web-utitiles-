package com.mwu.routing;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * HTTP Request wrapper with modern conveniences
 */
public class Request {
    private String method;
    private String path;
    private String queryString;
    private String protocol;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private Map<String, String> pathParams;
    private byte[] body;
    private String bodyString;
    private Socket socket;
    
    // Request context for middleware
    private Map<String, Object> attributes;
    
    private Request() {
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
        this.pathParams = new HashMap<>();
        this.attributes = new HashMap<>();
    }
    
    public static Request parse(InputStream input, Socket socket) throws IOException {
        Request request = new Request();
        request.socket = socket;
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        
        // Parse request line
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty request");
        }
        
        String[] parts = requestLine.split(" ");
        if (parts.length < 3) {
            throw new IOException("Invalid request line: " + requestLine);
        }
        
        request.method = parts[0];
        String fullPath = parts[1];
        request.protocol = parts[2];
        
        // Parse path and query string
        int queryIndex = fullPath.indexOf('?');
        if (queryIndex != -1) {
            request.path = fullPath.substring(0, queryIndex);
            request.queryString = fullPath.substring(queryIndex + 1);
            request.parseQueryString(request.queryString);
        } else {
            request.path = fullPath;
        }
        
        // Parse headers
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int colonIndex = line.indexOf(':');
            if (colonIndex != -1) {
                String headerName = line.substring(0, colonIndex).trim();
                String headerValue = line.substring(colonIndex + 1).trim();
                request.headers.put(headerName.toLowerCase(), headerValue);
            }
        }
        
        // Parse body if present
        String contentLengthStr = request.headers.get("content-length");
        if (contentLengthStr != null) {
            try {
                int contentLength = Integer.parseInt(contentLengthStr);
                if (contentLength > 0) {
                    char[] bodyChars = new char[contentLength];
                    int totalRead = 0;
                    while (totalRead < contentLength) {
                        int read = reader.read(bodyChars, totalRead, contentLength - totalRead);
                        if (read == -1) break;
                        totalRead += read;
                    }
                    request.bodyString = new String(bodyChars, 0, totalRead);
                    request.body = request.bodyString.getBytes(StandardCharsets.UTF_8);
                }
            } catch (NumberFormatException e) {
                // Invalid content length, skip body
            }
        }
        
        return request;
    }
    
    private void parseQueryString(String query) {
        if (query == null || query.isEmpty()) return;
        
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int eqIndex = pair.indexOf('=');
            if (eqIndex != -1) {
                try {
                    String key = URLDecoder.decode(pair.substring(0, eqIndex), "UTF-8");
                    String value = URLDecoder.decode(pair.substring(eqIndex + 1), "UTF-8");
                    queryParams.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    // Skip invalid pairs
                }
            }
        }
    }
    
    // ==================== Getters ====================
    
    public String getMethod() {
        return method;
    }
    
    public String getPath() {
        return path;
    }
    
    public String getQueryString() {
        return queryString;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }
    
    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }
    
    public String getQueryParam(String name) {
        return queryParams.get(name);
    }
    
    public String getQueryParam(String name, String defaultValue) {
        return queryParams.getOrDefault(name, defaultValue);
    }
    
    public Map<String, String> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }
    
    public String getPathParam(String name) {
        return pathParams.get(name);
    }
    
    public Map<String, String> getPathParams() {
        return Collections.unmodifiableMap(pathParams);
    }
    
    public void setParams(Map<String, String> params) {
        this.pathParams = params;
    }
    
    public byte[] getBody() {
        return body;
    }
    
    public String getBodyAsString() {
        return bodyString;
    }
    
    public String getClientIP() {
        // Check for X-Forwarded-For header first
        String forwarded = getHeader("x-forwarded-for");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        
        // Fall back to socket address
        if (socket != null) {
            return socket.getInetAddress().getHostAddress();
        }
        
        return "unknown";
    }
    
    public String getContentType() {
        return getHeader("content-type");
    }
    
    public boolean isJson() {
        String contentType = getContentType();
        return contentType != null && contentType.contains("application/json");
    }
    
    public boolean isFormData() {
        String contentType = getContentType();
        return contentType != null && contentType.contains("application/x-www-form-urlencoded");
    }
    
    // ==================== Request Context ====================
    
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    // ==================== Convenience Methods ====================
    
    public boolean accepts(String contentType) {
        String accept = getHeader("accept");
        return accept != null && accept.contains(contentType);
    }
    
    public boolean isSecure() {
        String proto = getHeader("x-forwarded-proto");
        return proto != null && proto.equals("https");
    }
    
    public String getUserAgent() {
        return getHeader("user-agent");
    }
    
    @Override
    public String toString() {
        return String.format("%s %s %s", method, path, protocol);
    }
}