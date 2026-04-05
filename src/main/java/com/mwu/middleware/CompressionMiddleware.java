package com.mwu.middleware;

import com.mwu.routing.Request;
import com.mwu.routing.Response;

/**
 * Response compression middleware (placeholder)
 */
public class CompressionMiddleware implements Middleware {
    @Override
    public void handle(Request request, Response response, Runnable next) throws Exception {
        // TODO: Implement gzip compression
        // For now, just pass through
        next.run();
    }
}