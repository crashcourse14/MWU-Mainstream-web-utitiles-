package com.mwu.middleware;

import com.mwu.routing.Request;
import com.mwu.routing.Response;
import java.util.UUID;

/**
 * Adds a unique request ID for tracing
 */
public class RequestIdMiddleware implements Middleware {
    @Override
    public void handle(Request request, Response response, Runnable next) throws Exception {
        String requestId = UUID.randomUUID().toString();
        request.setAttribute("requestId", requestId);
        response.header("X-Request-ID", requestId);
        
        next.run();
    }
}