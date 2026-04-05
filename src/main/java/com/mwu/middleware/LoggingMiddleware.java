package com.mwu.middleware;

import com.mwu.routing.Request;
import com.mwu.routing.Response;
import com.mwu.logger.Logger;

/**
 * Logs all requests with timing information
 */
public class LoggingMiddleware implements Middleware {
    private Logger logger = new Logger();
    
    @Override
    public void handle(Request request, Response response, Runnable next) throws Exception {
        long start = System.currentTimeMillis();
        
        try {
            next.run();
        } finally {
            long duration = System.currentTimeMillis() - start;
            logger.log(String.format("%s %s - %d (%dms)", 
                request.getMethod(), 
                request.getPath(), 
                response.getStatusCode(),
                duration
            ));
        }
    }
}