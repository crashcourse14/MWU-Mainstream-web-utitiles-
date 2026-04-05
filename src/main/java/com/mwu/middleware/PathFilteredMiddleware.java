package com.mwu.middleware;

import com.mwu.routing.Request;
import com.mwu.routing.Response;

/**
 * Middleware that only executes for specific path prefixes
 */
public class PathFilteredMiddleware implements Middleware {
    private final String pathPrefix;
    private final Middleware middleware;
    
    public PathFilteredMiddleware(String pathPrefix, Middleware middleware) {
        this.pathPrefix = pathPrefix;
        this.middleware = middleware;
    }
    
    @Override
    public void handle(Request request, Response response, Runnable next) throws Exception {
        if (request.getPath().startsWith(pathPrefix)) {
            middleware.handle(request, response, next);
        } else {
            next.run();
        }
    }
}