package com.mwu.middleware;

import com.mwu.routing.Request;
import com.mwu.routing.Response;

/**
 * CORS (Cross-Origin Resource Sharing) middleware
 */
public class CorsMiddleware implements Middleware {
    private final String allowedOrigins;
    private final String allowedMethods;
    private final String allowedHeaders;
    
    public CorsMiddleware() {
        this("*", "GET, POST, PUT, DELETE, PATCH, OPTIONS", "*");
    }
    
    public CorsMiddleware(String origins, String methods, String headers) {
        this.allowedOrigins = origins;
        this.allowedMethods = methods;
        this.allowedHeaders = headers;
    }
    
    @Override
    public void handle(Request request, Response response, Runnable next) throws Exception {
        response.header("Access-Control-Allow-Origin", allowedOrigins);
        response.header("Access-Control-Allow-Methods", allowedMethods);
        response.header("Access-Control-Allow-Headers", allowedHeaders);
        response.header("Access-Control-Max-Age", "86400");
        
        if ("OPTIONS".equals(request.getMethod())) {
            response.status(204).send();
            return;
        }
        
        next.run();
    }
}