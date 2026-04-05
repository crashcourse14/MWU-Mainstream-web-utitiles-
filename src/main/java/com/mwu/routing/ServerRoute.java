package com.mwu.routing;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerRoute {
    private final String method;
    private final String path;
    private final RouteHandler handler;
    private Map<String, String> params;
    private final Pattern pathPattern;

    public ServerRoute(String method, String path, RouteHandler handler) {
        this.method = method;
        this.path = path;
        this.handler = handler;
        this.params = new HashMap<>();
        this.pathPattern = compilePathPattern(path);
    }

    private Pattern compilePathPattern(String path) {
        // Convert path like "/users/:id/posts/:postId" to regex pattern
        String regex = path.replaceAll(":[\\w]+", "([\\w-]+)");
        regex = "^" + regex + "$";
        return Pattern.compile(regex);
    }

    public boolean matches(String path) {
        return pathPattern.matcher(path).matches();
    }

    public Map<String, String> extractParams(String path) {
        Map<String, String> extractedParams = new HashMap<>();
        Matcher matcher = pathPattern.matcher(path);
        
        if (matcher.matches()) {
            // Extract parameter names from original path
            Pattern paramPattern = Pattern.compile(":(\\w+)");
            Matcher paramMatcher = paramPattern.matcher(this.path);
            
            int groupIndex = 1;
            while (paramMatcher.find()) {
                String paramName = paramMatcher.group(1);
                String paramValue = matcher.group(groupIndex);
                extractedParams.put(paramName, paramValue);
                groupIndex++;
            }
        }
        
        return extractedParams;
    }

    // Getters and setters
    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public RouteHandler getHandler() {
        return handler;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getPattern() {
        return path;
    }
}