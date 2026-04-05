package com.mwu.routing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Advanced router with pattern matching, route parameters, and route groups
 */
public class Router {
    private final Map<String, List<ServerRoute>> routes;
    private final Map<String, WebSocketHandler> webSocketRoutes;
    private String prefix = "";
    
    public Router() {
        this.routes = new ConcurrentHashMap<>();
        this.webSocketRoutes = new ConcurrentHashMap<>();
    }
    
    private Router(String prefix) {
        this();
        this.prefix = prefix;
    }
    
    // ==================== Route Registration ====================
    
    public void addRoute(String method, String path, RouteHandler handler) {
        String fullPath = prefix + path;
        ServerRoute route = new ServerRoute(method, fullPath, handler);
        
        routes.computeIfAbsent(method, k -> new ArrayList<>()).add(route);
    }
    
    public void addWebSocketRoute(String path, WebSocketHandler handler) {
        String fullPath = prefix + path;
        webSocketRoutes.put(fullPath, handler);
    }
    
    // ==================== Route Matching ====================
    
    public ServerRoute match(String method, String path) {
        List<ServerRoute> list = routes.get(method);
        if (list == null) return null;

        for (ServerRoute route : list) {
            if (route.matches(path)) {
                route.setParams(route.extractParams(path));
                return route;
            }
        }
        return null;
    }
    
    public WebSocketHandler matchWebSocket(String path) {
        return webSocketRoutes.get(path);
    }
    
    // ==================== Route Groups ====================
    
    public Router group(String prefix) {
        return new Router(this.prefix + prefix);
    }
    
    // ==================== Info ====================
    
    public int getRouteCount() {
        return routes.values().stream().mapToInt(List::size).sum();
    }
    
    public List<String> getAllRoutes() {
        List<String> allRoutes = new ArrayList<>();
        
        for (Map.Entry<String, List<ServerRoute>> entry : routes.entrySet()) {
            String method = entry.getKey();
            for (ServerRoute route : entry.getValue()) {
                allRoutes.add(method + " " + route.getPattern());
            }
        }
        
        return allRoutes;
    }
}

/**
 * Represents a single route with pattern matching
 */
class Route {
    private final String method;
    private final String pattern;
    private final RouteHandler handler;
    private final Pattern compiledPattern;
    private final List<String> paramNames;
    
    public Route(String method, String pattern, RouteHandler handler) {
        this.method = method;
        this.pattern = pattern;
        this.handler = handler;
        this.paramNames = new ArrayList<>();
        this.compiledPattern = compilePattern(pattern);
    }
    
    private Pattern compilePattern(String pattern) {
        // Convert route pattern to regex
        // Example: /users/:id -> /users/([^/]+)
        //          /files/*path -> /files/(.*)
        
        StringBuilder regex = new StringBuilder("^");
        String[] segments = pattern.split("/");
        
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            
            if (segment.isEmpty() && i == 0) {
                continue; // Skip leading slash
            }
            
            regex.append("/");
            
            if (segment.startsWith(":")) {
                // Named parameter
                String paramName = segment.substring(1);
                paramNames.add(paramName);
                regex.append("([^/]+)");
            } else if (segment.equals("*") || segment.startsWith("*")) {
                // Wildcard
                if (segment.length() > 1) {
                    paramNames.add(segment.substring(1));
                } else {
                    paramNames.add("wildcard");
                }
                regex.append("(.*)");
            } else {
                // Literal segment
                regex.append(Pattern.quote(segment));
            }
        }
        
        regex.append("$");
        return Pattern.compile(regex.toString());
    }
    
    public boolean matches(String path) {
        return compiledPattern.matcher(path).matches();
    }
    
    public Map<String, String> getParams() {
        return new HashMap<>(); // Will be populated during matching
    }
    
    public Map<String, String> extractParams(String path) {
        Map<String, String> params = new HashMap<>();
        Matcher matcher = compiledPattern.matcher(path);
        
        if (matcher.matches()) {
            for (int i = 0; i < paramNames.size() && i < matcher.groupCount(); i++) {
                params.put(paramNames.get(i), matcher.group(i + 1));
            }
        }
        
        return params;
    }
    
    public String getMethod() {
        return method;
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public RouteHandler getHandler() {
        return handler;
    }
}