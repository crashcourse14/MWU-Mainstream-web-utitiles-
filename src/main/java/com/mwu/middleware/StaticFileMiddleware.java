package com.mwu.middleware;

import com.mwu.routing.Request;
import com.mwu.routing.Response;

/**
 * Static file serving middleware (placeholder)
 */
public class StaticFileMiddleware implements Middleware {
    private final String directory;
    
    public StaticFileMiddleware(String directory) {
        this.directory = directory;
    }
    
    @Override
    public void handle(Request request, Response response, Runnable next) throws Exception {
        // TODO: Implement static file serving
        next.run();
    }
}