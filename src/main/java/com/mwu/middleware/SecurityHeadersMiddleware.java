package com.mwu.middleware;

import com.mwu.routing.Request;
import com.mwu.routing.Response;

/**
 * Adds security headers to all responses
 */
public class SecurityHeadersMiddleware implements Middleware {
    @Override
    public void handle(Request request, Response response, Runnable next) throws Exception {
        response.header("X-Content-Type-Options", "nosniff");
        response.header("X-Frame-Options", "DENY");
        response.header("X-XSS-Protection", "1; mode=block");
        response.header("Referrer-Policy", "strict-origin-when-cross-origin");
        
        next.run();
    }
}